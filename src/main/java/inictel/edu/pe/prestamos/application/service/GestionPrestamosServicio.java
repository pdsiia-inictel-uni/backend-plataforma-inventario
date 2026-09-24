package inictel.edu.pe.prestamos.application.service;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.prestamos.application.comando.RegistrarDevolucionComando;
import inictel.edu.pe.prestamos.application.comando.RegistrarPrestamoComando;
import inictel.edu.pe.prestamos.application.dto.DestinatarioDto;
import inictel.edu.pe.prestamos.application.dto.PrestamoDto;
import inictel.edu.pe.prestamos.domain.model.BienPrestado;
import inictel.edu.pe.prestamos.domain.model.CoordinacionDestino;
import inictel.edu.pe.prestamos.domain.model.Destinatario;
import inictel.edu.pe.prestamos.domain.model.OperadorPrestamo;
import inictel.edu.pe.prestamos.domain.model.PersonaResponsable;
import inictel.edu.pe.prestamos.domain.model.Prestamo;
import inictel.edu.pe.prestamos.domain.repository.FiltroPrestamos;
import inictel.edu.pe.prestamos.domain.repository.PrestamoRepositorio;
import inictel.edu.pe.prestamos.domain.service.CatalogoBienes;
import inictel.edu.pe.prestamos.domain.service.DirectorioCoordinaciones;
import inictel.edu.pe.prestamos.domain.service.DirectorioResponsables;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Casos de uso de prestamos y devoluciones (RF-58 .. RF-68).
 *
 * <p>Coordina dos contextos: registra el prestamo en su propio agregado y pide
 * a inventario que actualice la condicion del bien, siempre a traves del puerto
 * {@link CatalogoBienes}.</p>
 *
 * <p>El Administrador no participa: consulta, pero no entrega ni recibe bienes
 * (RN-22).</p>
 */
@Service
public class GestionPrestamosServicio {


    private final PrestamoRepositorio prestamos;
    private final CatalogoBienes catalogo;
    private final DirectorioResponsables directorio;
    private final DirectorioCoordinaciones coordinaciones;
    private final ContextoUsuario contexto;

    public GestionPrestamosServicio(PrestamoRepositorio prestamos,
                                    CatalogoBienes catalogo,
                                    DirectorioResponsables directorio,
                                    DirectorioCoordinaciones coordinaciones,
                                    ContextoUsuario contexto) {
        this.prestamos = prestamos;
        this.catalogo = catalogo;
        this.directorio = directorio;
        this.coordinaciones = coordinaciones;
        this.contexto = contexto;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Pagina<PrestamoDto> buscar(FiltroPrestamos filtro, CriterioPagina criterio) {
        return prestamos.buscar(acotar(filtro), criterio).mapear(PrestamoDto::de);
    }

    @Transactional(readOnly = true)
    public PrestamoDto obtener(Long id) {
        Prestamo prestamo = exigirPrestamo(id);
        exigirLectura(prestamo);
        return PrestamoDto.de(prestamo);
    }

    /** RF-66: historial de prestamos de un bien. */
    @Transactional(readOnly = true)
    public List<PrestamoDto> historialPorBien(Long equipoId) {
        BienPrestado bien = catalogo.buscar(equipoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el bien", equipoId));

        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.puedeLeerCoordinacion(bien.coordinacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a los préstamos de otra coordinación.");
        }
        return prestamos.historialPorBien(equipoId).stream().map(PrestamoDto::de).toList();
    }

    /** RF-66: historial de prestamos de una persona, dentro del ambito permitido. */
    @Transactional(readOnly = true)
    public List<PrestamoDto> historialPorPersona(String dni, Long coordinacionId) {
        Long ambito = ambitoDeConsulta(coordinacionId);
        return prestamos.historialPorPersona(dni.trim(), ambito).stream().map(PrestamoDto::de).toList();
    }

    /**
     * RF-59: coordinaciones a las que puede ir un equipo prestado: todas las de
     * la institucion, de su misma Direccion o de otra. La lista general de
     * coordinaciones esta acotada a la propia (RN-23), por eso los prestamos
     * tienen la suya, que solo lleva nombres.
     */
    @Transactional(readOnly = true)
    public List<CoordinacionDestino> coordinacionesDestino() {
        contexto.requerido();
        return coordinaciones.todas();
    }

    /**
     * RF-59: a quien puede entregarse un equipo en una coordinacion de destino:
     * su Responsable y sus Operadores activos. La coordinacion de destino puede
     * ser cualquiera de la institucion, no solo la propia.
     */
    @Transactional(readOnly = true)
    public List<DestinatarioDto> destinatarios(Long coordinacionId) {
        contexto.requerido();
        if (coordinacionId == null) {
            throw new DatosInvalidosException("coordinacionId", "Seleccione la coordinación de destino.");
        }
        return directorio.destinatariosDe(coordinacionId).stream().map(DestinatarioDto::de).toList();
    }

    /** RF-67: prestamos activos vencidos. */
    @Transactional(readOnly = true)
    public List<PrestamoDto> vencidos(Long coordinacionId) {
        return prestamos.vencidos(ambitoDeConsulta(coordinacionId)).stream().map(PrestamoDto::de).toList();
    }

    // ------------------------------------------------------------------
    // RF-59, RF-60: registro del prestamo
    // ------------------------------------------------------------------

    @Transactional
    public PrestamoDto registrar(RegistrarPrestamoComando comando) {
        UsuarioAutenticado actual = exigirOperativo();

        BienPrestado bien = catalogo.buscar(comando.equipoId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("el bien", comando.equipoId()));

        // RN-23: no se presta un bien de otra coordinacion.
        actual.exigirAccesoA(bien.coordinacionId());

        exigirResponsableVigente(bien.coordinacionId());
        validarDisponibilidad(comando.equipoId());

        String coordinacionDestino = coordinaciones.nombreDe(comando.coordinacionDestinoId())
                .orElseThrow(() -> new DatosInvalidosException("coordinacionDestinoId",
                        "La coordinación de destino no existe."));
        Destinatario destinatario = exigirDestinatario(comando.personaUsuarioId(),
                comando.coordinacionDestinoId(), coordinacionDestino);

        Prestamo prestamo = Prestamo.registrar(
                bien,
                new PersonaResponsable(destinatario.nombreCompleto(), destinatario.dni(),
                        destinatario.id(), destinatario.coordinacionId()),
                coordinacionDestino,
                comando.fechaEstimadaDevolucion(),
                comando.observacionesSalida(),
                operadorActual());

        Prestamo guardado = prestamos.guardar(prestamo);

        String persona = guardado.getPersona().nombre() + " (DNI " + guardado.getPersona().dni() + ")";
        catalogo.marcarPrestado(bien.id(), "Entregado a " + persona + ".");

        return PrestamoDto.de(guardado);
    }

    // ------------------------------------------------------------------
    // RF-61 .. RF-65: devolucion
    // ------------------------------------------------------------------

    @Transactional
    public PrestamoDto registrarDevolucion(RegistrarDevolucionComando comando) {
        UsuarioAutenticado actual = exigirOperativo();
        Prestamo prestamo = exigirPrestamo(comando.prestamoId());
        actual.exigirAccesoA(prestamo.coordinacionId());
        exigirResponsableVigente(prestamo.coordinacionId());

        prestamo.registrarDevolucion(
                Boolean.TRUE.equals(comando.conforme()),
                Boolean.TRUE.equals(comando.reportaDano()),
                comando.observacionesRetorno(),
                operadorActual());

        Prestamo guardado = prestamos.guardar(prestamo);

        // RN-19: el dano marca el bien para revision, no lo retira del servicio.
        String detalle = guardado.isReportaDano()
                ? "Devuelto por " + guardado.getPersona().nombre()
                        + " con observaciones. Queda pendiente de revisión del responsable."
                : "Devuelto conforme por " + guardado.getPersona().nombre() + ".";
        catalogo.registrarRetorno(guardado.getBien().id(), guardado.isReportaDano(), detalle);

        String resultado = guardado.isReportaDano()
                ? "Operativo, pendiente de revisión del responsable"
                : "Operativo";
        return PrestamoDto.de(guardado);
    }

    // ------------------------------------------------------------------
    // Autorizacion y ambito (RN-22, RN-23)
    // ------------------------------------------------------------------

    /**
     * RN-07: una Coordinacion sin Responsable no opera.
     *
     * <p>Un equipo sale y vuelve a nombre de quien responde por el inventario.
     * Mientras el cargo esta vacante —porque se dio de baja al vigente y aun no
     * hay sucesor (RF-26)— no hay quien responda, y la coordinacion queda
     * parada: ni salidas ni devoluciones. Es visible desde antes, en su tarjeta
     * marcada en rojo, y se levanta nombrando a alguien.</p>
     */
    private void exigirResponsableVigente(Long coordinacionId) {
        if (!directorio.tieneResponsableVigente(coordinacionId)) {
            throw new ReglaNegocioException(
                    "Su coordinación no tiene responsable en este momento, asi que no puede "
                            + "registrar movimientos de equipos. Pida al Administrador que nombre uno.");
        }
    }

    /**
     * RF-59: el equipo se entrega a un Responsable u Operador activo de la
     * coordinacion de destino, y a nadie mas.
     */
    private Destinatario exigirDestinatario(Long usuarioId, Long coordinacionId, String coordinacion) {
        return directorio.destinatario(usuarioId)
                .filter(d -> Objects.equals(d.coordinacionId(), coordinacionId))
                .orElseThrow(() -> new DatosInvalidosException("personaUsuarioId",
                        "La persona elegida no es un responsable u operador activo de "
                                + coordinacion + ". Elija a alguien de la lista."));
    }

    private FiltroPrestamos acotar(FiltroPrestamos filtro) {
        Long ambito = ambitoDeConsulta(filtro.coordinacionId());
        return ambito == null ? filtro : filtro.enCoordinacion(ambito);
    }

    /**
     * Coordinacion sobre la que se consulta: la propia para un usuario
     * operativo; la elegida —o toda la institucion— para el Administrador.
     * Pedir explicitamente otra Coordinacion se rechaza (RNF-10).
     */
    private Long ambitoDeConsulta(Long solicitada) {
        return contexto.requerido().ambitoDeConsulta(solicitada);
    }

    private void exigirLectura(Prestamo prestamo) {
        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.puedeLeerCoordinacion(prestamo.coordinacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a los préstamos de otra coordinación.");
        }
    }

    /** RF-68: el Administrador consulta prestamos, pero no los mueve. */
    private UsuarioAutenticado exigirOperativo() {
        UsuarioAutenticado actual = contexto.requerido();
        if (actual.esAdmin()) {
            throw new AccesoDenegadoException(
                    "El Administrador no registra préstamos ni devoluciones. Esa gestión corresponde al "
                            + "responsable y a los operadores de cada coordinación.");
        }
        return actual;
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private Prestamo exigirPrestamo(Long id) {
        return prestamos.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el préstamo", id));
    }

    /** RN-15, RN-16: solo sale un bien operativo, activo y sin prestamo vigente. */
    private void validarDisponibilidad(Long equipoId) {
        if (catalogo.estaDeBaja(equipoId)) {
            throw new ReglaNegocioException("El bien esta dado de baja y no puede prestarse.");
        }
        if (prestamos.tienePrestamoActivo(equipoId)) {
            throw new ReglaNegocioException(
                    "El bien ya se encuentra prestado. Registre su devolución antes de volver a prestarlo.");
        }
        if (!catalogo.estaDisponible(equipoId)) {
            throw new ReglaNegocioException(
                    "Solo pueden prestarse bienes disponibles. Condición actual: "
                            + catalogo.condicionActual(equipoId) + ".");
        }
    }

    private OperadorPrestamo operadorActual() {
        UsuarioAutenticado actual = contexto.requerido();
        return new OperadorPrestamo(actual.id(), actual.nombreCompleto());
    }
}
