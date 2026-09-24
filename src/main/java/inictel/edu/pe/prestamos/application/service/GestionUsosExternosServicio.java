package inictel.edu.pe.prestamos.application.service;

import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.prestamos.application.comando.AbrirUsoExternoComando;
import inictel.edu.pe.prestamos.application.comando.CerrarUsoExternoComando;
import inictel.edu.pe.prestamos.application.dto.UsoExternoDto;
import inictel.edu.pe.prestamos.domain.model.BienPrestado;
import inictel.edu.pe.prestamos.domain.model.OperadorPrestamo;
import inictel.edu.pe.prestamos.domain.model.UsoExterno;
import inictel.edu.pe.prestamos.domain.repository.UsoExternoRepositorio;
import inictel.edu.pe.prestamos.domain.service.CatalogoBienes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Casos de uso del registro de uso externo (RF-78).
 *
 * <p>Un equipo se presta para que lo use, dentro de la institucion, personal
 * de otra institucion con su encargado. El registro se hace en dos partes y
 * queda guardado como historia del equipo:</p>
 * <ul>
 *   <li><b>apertura</b> (puntos 1 a 5): el equipo pasa a <i>Prestado</i> y el
 *       historial del bien lo anota;</li>
 *   <li><b>cierre</b> (puntos 6 a 10): el equipo vuelve a <i>Operativo</i>, con
 *       revision pendiente si no volvio en condiciones operativas (RN-19).</li>
 * </ul>
 *
 * <p>Lo gestiona el <b>Responsable</b> de la coordinacion del equipo: es quien
 * responde por el bien que sale y quien firma el formato (RF-80, RN-23).</p>
 */
@Service
public class GestionUsosExternosServicio {

    private final UsoExternoRepositorio usos;
    private final CatalogoBienes catalogo;
    private final ContextoUsuario contexto;

    public GestionUsosExternosServicio(UsoExternoRepositorio usos,
                                       CatalogoBienes catalogo,
                                       ContextoUsuario contexto) {
        this.usos = usos;
        this.catalogo = catalogo;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public List<UsoExternoDto> historialPorBien(Long equipoId) {
        BienPrestado bien = exigirBien(equipoId);
        exigirLectura(bien.coordinacionId());
        return usos.historialPorBien(equipoId).stream().map(UsoExternoDto::de).toList();
    }

    @Transactional(readOnly = true)
    public UsoExternoDto obtener(Long id) {
        UsoExterno uso = exigirUso(id);
        exigirLectura(uso.coordinacionId());
        return UsoExternoDto.de(uso);
    }

    /** Primera parte: puntos 1 a 5. El equipo pasa a Prestado. */
    @Transactional
    public UsoExternoDto abrir(Long equipoId, AbrirUsoExternoComando c) {
        UsuarioAutenticado actual = exigirResponsable();
        BienPrestado bien = exigirBien(equipoId);
        actual.exigirAccesoA(bien.coordinacionId());

        if (catalogo.estaDeBaja(equipoId)) {
            throw new ReglaNegocioException("El equipo está dado de baja y no puede prestarse.");
        }
        if (usos.tieneAbierto(equipoId)) {
            throw new ReglaNegocioException(
                    "El equipo ya tiene un uso externo abierto. Ciérrelo antes de registrar otro.");
        }
        if (!catalogo.estaDisponible(equipoId)) {
            throw new ReglaNegocioException(
                    "Solo se presta un equipo disponible. Condición actual: "
                            + catalogo.condicionActual(equipoId) + ".");
        }

        // Solo sale un equipo operativo (lo comprueba lo de arriba); el formato
        // lo dice con el vocabulario del inventario, no con el de prestamos.
        UsoExterno uso = UsoExterno.abrir(bien, "Operativo",
                c.encargadoNombre(), c.encargadoCorreo(), c.encargadoCelular(),
                c.usuarioNombre(), c.usuarioCorreo(), c.usuarioTelefono(),
                c.proyecto(), c.fechaInicio(), c.horaInicio(), c.fechaFinPrevista(), c.horaFinPrevista(), c.actividad(),
                operador(actual));
        UsoExterno guardado = usos.guardar(uso);

        catalogo.marcarPrestado(equipoId, "Uso externo (registro N.º " + guardado.getId()
                + "): a cargo de " + guardado.getEncargadoNombre()
                + ". Lo usa " + guardado.getUsuarioNombre() + ".");
        return UsoExternoDto.de(guardado);
    }

    /** Parte final: puntos 6 a 10. El equipo vuelve al servicio. */
    @Transactional
    public UsoExternoDto cerrar(Long id, CerrarUsoExternoComando c) {
        UsuarioAutenticado actual = exigirResponsable();
        UsoExterno uso = exigirUso(id);
        actual.exigirAccesoA(uso.coordinacionId());

        uso.cerrar(c.entregadoOperativo(), c.devueltoOperativo(), c.incidente(),
                c.accionCorrectiva(), c.observaciones(), operador(actual));
        UsoExterno guardado = usos.guardar(uso);

        // RN-19: si volvio mal, queda para revision; el Responsable decide si
        // pasa a mantenimiento.
        String detalle = guardado.reportaDano()
                ? "Fin del uso externo (registro N.º " + guardado.getId()
                        + "): devuelto con observaciones. Queda pendiente de revisión."
                : "Fin del uso externo (registro N.º " + guardado.getId() + "): devuelto conforme.";
        catalogo.registrarRetorno(guardado.getBien().id(), guardado.reportaDano(), detalle);
        return UsoExternoDto.de(guardado);
    }

    /**
     * Anula un uso que solo tiene su primera parte: se cancelo y el equipo no
     * llego a usarse. Borra el registro y devuelve el equipo a Operativo. El
     * historial del equipo es inmutable (RN-21), asi que la salida y su
     * anulacion quedan anotadas en el.
     */
    @Transactional
    public void anular(Long id) {
        UsuarioAutenticado actual = exigirResponsable();
        UsoExterno uso = exigirUso(id);
        actual.exigirAccesoA(uso.coordinacionId());
        uso.exigirAnulable();

        usos.eliminar(id);
        catalogo.anularPrestamo(uso.getBien().id(),
                "Uso externo anulado (registro N.º " + id + "): se canceló y el equipo no llegó a usarse.");
    }

    // ------------------------------------------------------------------

    private UsuarioAutenticado exigirResponsable() {
        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.esResponsable()) {
            throw new AccesoDenegadoException(
                    "El uso externo de un equipo lo registra el responsable de su coordinación.");
        }
        return actual;
    }

    private void exigirLectura(Long coordinacionId) {
        if (!contexto.requerido().puedeLeerCoordinacion(coordinacionId)) {
            throw new AccesoDenegadoException("No tiene acceso a los equipos de otra coordinación.");
        }
    }

    private BienPrestado exigirBien(Long equipoId) {
        return catalogo.buscar(equipoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el bien", equipoId));
    }

    private UsoExterno exigirUso(Long id) {
        return usos.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el registro de uso", id));
    }

    private OperadorPrestamo operador(UsuarioAutenticado actual) {
        return new OperadorPrestamo(actual.id(), actual.nombreCompleto());
    }
}
