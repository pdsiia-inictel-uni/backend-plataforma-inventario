package inictel.edu.pe.inventario.application.service;

import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.inventario.application.dto.BienPrestableDto;
import inictel.edu.pe.inventario.application.dto.EquipoDto;
import inictel.edu.pe.inventario.application.dto.ResumenBienesDto;
import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.Equipo;
import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;
import inictel.edu.pe.inventario.domain.model.TipoMovimiento;
import inictel.edu.pe.inventario.domain.repository.EquipoRepositorio;
import inictel.edu.pe.inventario.domain.repository.FiltroEquipos;
import inictel.edu.pe.inventario.domain.repository.MovimientoRepositorio;
import inictel.edu.pe.inventario.domain.service.DirectorioUsuarios;
import inictel.edu.pe.inventario.domain.service.UbicacionesDisponibles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de host abierto del contexto inventario: la unica puerta por la que
 * prestamos, organizacion y reportes acceden a los bienes.
 *
 * <p>Expone operaciones deliberadamente estrechas —consultar, marcar prestado,
 * registrar el retorno y obtener cifras— para que ningun otro contexto
 * manipule el agregado directamente (RNF-39).</p>
 */
@Service
public class ServicioPublicoInventario {

    private final EquipoRepositorio equipos;
    private final MovimientoRepositorio movimientos;
    private final DirectorioUsuarios directorio;
    private final UbicacionesDisponibles ubicaciones;
    private final ContextoUsuario contexto;

    public ServicioPublicoInventario(EquipoRepositorio equipos,
                                     MovimientoRepositorio movimientos,
                                     DirectorioUsuarios directorio,
                                     UbicacionesDisponibles ubicaciones,
                                     ContextoUsuario contexto) {
        this.equipos = equipos;
        this.movimientos = movimientos;
        this.directorio = directorio;
        this.ubicaciones = ubicaciones;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public Optional<BienPrestableDto> buscarBien(Long equipoId) {
        return equipos.buscarPorId(equipoId).map(BienPrestableDto::de);
    }

    /**
     * Ficha completa de un bien, con su ubicacion y sus personas resueltas.
     *
     * <p>La usan los documentos que describen un equipo concreto —el formato
     * de registro de uso (RF-78)— para tomar sus datos del sistema en lugar de
     * pedirselos otra vez a quien ya los tiene registrados. Es una consulta:
     * no cambia nada del bien.</p>
     */
    @Transactional(readOnly = true)
    public Optional<EquipoDto> fichaDe(Long equipoId) {
        return equipos.buscarPorId(equipoId)
                .map(equipo -> EquipoDto.de(equipo,
                        ubicaciones.nombreDeCoordinacion(equipo.getCoordinacionId()).orElse(null),
                        ubicaciones.nombreDeLaboratorio(equipo.getLaboratorioId()).orElse(null),
                        directorio.nombreDe(equipo.getResponsableId()).orElse(null),
                        nombreDeQuienLoTieneACargo(equipo),
                        directorio.nombreDe(equipo.getUsuarioRegistroId()).orElse(null)));
    }

    /**
     * RF-60: el bien pasa a Prestado y el hecho queda en su historial.
     *
     * <p>El agregado valida que estuviera operativo; el movimiento deja
     * constancia de quien lo entrego y a quien (RF-53).</p>
     */
    @Transactional
    public void marcarPrestado(Long equipoId, String detalle) {
        Equipo equipo = exigirEquipo(equipoId);
        CondicionEquipo anterior = equipo.getCondicion();
        equipo.marcarComoPrestado();
        equipos.guardar(equipo);

        registrarMovimiento(equipo, TipoMovimiento.PRESTAMO, anterior, detalle);
    }

    /**
     * RF-63, RF-64: el bien vuelve a Operativo; si se reporto dano queda
     * marcado para que el Responsable decida (RN-19).
     */
    @Transactional
    public void registrarRetorno(Long equipoId, boolean reportaDano, String detalle) {
        Equipo equipo = exigirEquipo(equipoId);
        CondicionEquipo anterior = equipo.getCondicion();
        equipo.registrarRetorno(reportaDano);
        equipos.guardar(equipo);

        registrarMovimiento(equipo, TipoMovimiento.DEVOLUCION, anterior, detalle);
    }

    // ------------------------------------------------------------------
    // RF-83, RN-38: bienes a cargo de una persona
    // ------------------------------------------------------------------

    /**
     * Cuantos bienes en servicio tiene esa persona a su nombre.
     *
     * <p>Lo pregunta {@code iam} antes de dar de baja a alguien o de retirarlo
     * de su Coordinacion: quien tiene equipos a su cargo no puede irse
     * dejandolos sin nadie que responda por ellos. El Responsable debe
     * reasignarlos antes —a otro Operador o a si mismo— (RN-38).</p>
     */
    @Transactional(readOnly = true)
    public long contarBienesACargoDe(Long usuarioId) {
        return equipos.contarACargoDe(usuarioId);
    }

    /**
     * Los bienes en servicio a nombre de esa persona, nombrados uno a uno.
     *
     * <p>Los pide {@code iam} para poder decir, cuando la baja de un Operador
     * se rechaza, <b>cuales</b> son los equipos que lo retienen y donde estan.
     * Con el numero a secas el Responsable tenia que ir a buscarlos al
     * inventario para saber que reasignar (RN-38, RNF-23).</p>
     */
    @Transactional(readOnly = true)
    public List<BienACargoDto> listarBienesACargoDe(Long usuarioId) {
        return equipos.listarACargoDe(usuarioId).stream()
                .map(equipo -> new BienACargoDto(
                        equipo.getId(),
                        equipo.getNombre(),
                        equipo.getCodigoInventario().valor(),
                        ubicaciones.nombreDeLaboratorio(equipo.getLaboratorioId()).orElse(null)))
                .toList();
    }

    /** Lo justo para reconocer un equipo que impide una baja (RN-38). */
    public record BienACargoDto(Long id, String nombre, String codigoInventario, String laboratorio) {
    }

    /**
     * Devuelve al Responsable de la Coordinacion todos los bienes que esa
     * persona tenia a su nombre.
     *
     * <p>No es una operacion que nadie pida desde una pantalla: la ejecuta
     * {@code iam} cuando un Operador de la Coordinacion <b>asciende a
     * Responsable</b> (RF-26b). Ahi los bienes no cambian de manos —los sigue
     * llevando la misma persona— pero dejan de estar asignados, porque quien
     * los tiene ya no es un Operador sino el Responsable, que es justamente lo
     * que significa no tener asignacion (RN-37).</p>
     */
    @Transactional
    public void devolverAlResponsableLosBienesDe(Long usuarioId) {
        for (Equipo equipo : equipos.listarACargoDe(usuarioId)) {
            equipo.devolverAlResponsableDeCoordinacion();
            Equipo guardado = equipos.guardar(equipo);
            registrarMovimiento(guardado, TipoMovimiento.RESPONSABLE, guardado.getCondicion(),
                    "El equipo queda a cargo del responsable de la coordinacion.");
        }
    }

    // ------------------------------------------------------------------
    // Cifras (RF-13, RF-15, RF-75 .. RF-77)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public long contarActivosEnCoordinacion(Long coordinacionId) {
        return equipos.contarActivosEnCoordinacion(coordinacionId);
    }

    @Transactional(readOnly = true)
    public long contarEnLaboratorio(Long laboratorioId) {
        return equipos.contarEnLaboratorio(laboratorioId);
    }

    /**
     * RF-75 .. RF-77: cifras para el panel de control y para el resumen de la
     * estructura organizacional.
     */
    @Transactional(readOnly = true)
    public ResumenBienesDto resumen(Long coordinacionId) {
        Map<CondicionEquipo, Long> conteo = equipos.contarPorCondicion(coordinacionId);
        long operativos = conteo.getOrDefault(CondicionEquipo.OPERATIVO, 0L);
        long prestados = conteo.getOrDefault(CondicionEquipo.PRESTADO, 0L);
        long mantenimiento = conteo.getOrDefault(CondicionEquipo.MANTENIMIENTO, 0L);
        long baja = conteo.getOrDefault(CondicionEquipo.BAJA, 0L);

        return new ResumenBienesDto(
                operativos + prestados + mantenimiento + baja,
                operativos,
                prestados,
                mantenimiento,
                baja,
                equipos.contarRevisionPendiente(coordinacionId),
                equipos.contarPorCategoria(coordinacionId));
    }

    /** RF-52: bienes filtrados para las exportaciones, ordenados por nombre. */
    @Transactional(readOnly = true)
    public List<EquipoDto> listarParaExportacion(FiltroEquipos filtro) {
        return equipos.listar(filtro).stream()
                .map(equipo -> EquipoDto.de(equipo,
                        ubicaciones.nombreDeCoordinacion(equipo.getCoordinacionId()).orElse(null),
                        ubicaciones.nombreDeLaboratorio(equipo.getLaboratorioId()).orElse(null),
                        directorio.nombreDe(equipo.getResponsableId()).orElse(null),
                        nombreDeQuienLoTieneACargo(equipo),
                        directorio.nombreDe(equipo.getUsuarioRegistroId()).orElse(null)))
                .toList();
    }

    /**
     * RF-83: quien responde por el bien hoy. El Operador a su cargo si lo
     * tiene; si no, el Responsable vigente de la Coordinacion, que es lo que
     * significa no tenerlo (RN-37).
     */
    private String nombreDeQuienLoTieneACargo(Equipo equipo) {
        if (equipo.estaACargoDeUnOperador()) {
            return directorio.nombreDe(equipo.getResponsableEquipoId()).orElse(null);
        }
        return directorio.responsableVigenteDe(equipo.getCoordinacionId())
                .flatMap(directorio::nombreDe)
                .orElse(null);
    }

    private void registrarMovimiento(Equipo equipo, TipoMovimiento tipo,
                                     CondicionEquipo anterior, String detalle) {
        movimientos.registrar(MovimientoEquipo.registrar(
                equipo.getId(), tipo, anterior, equipo.getCondicion(), detalle,
                contexto.actual().orElse(null)));
    }

    private Equipo exigirEquipo(Long id) {
        return equipos.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el bien", id));
    }
}
