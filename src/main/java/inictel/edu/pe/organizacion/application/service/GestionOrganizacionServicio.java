package inictel.edu.pe.organizacion.application.service;

import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.organizacion.application.dto.CoordinacionDto;
import inictel.edu.pe.organizacion.application.dto.DireccionDto;
import inictel.edu.pe.organizacion.application.dto.EstructuraDto;
import inictel.edu.pe.organizacion.application.dto.LaboratorioDto;
import inictel.edu.pe.organizacion.domain.model.Coordinacion;
import inictel.edu.pe.organizacion.domain.model.Direccion;
import inictel.edu.pe.organizacion.domain.model.Laboratorio;
import inictel.edu.pe.organizacion.domain.repository.CoordinacionRepositorio;
import inictel.edu.pe.organizacion.domain.repository.DireccionRepositorio;
import inictel.edu.pe.organizacion.domain.repository.LaboratorioRepositorio;
import inictel.edu.pe.organizacion.domain.service.CensoDeBienes;
import inictel.edu.pe.organizacion.domain.service.CensoDePersonal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Casos de uso de la estructura organizacional (RF-09 .. RF-15).
 *
 * <p>Todas las operaciones de escritura son exclusivas del Administrador; la
 * restriccion se declara en el controlador con {@code @PreAuthorize} y se
 * refuerza en la cadena de seguridad.</p>
 */
@Service
public class GestionOrganizacionServicio {

    private final DireccionRepositorio direcciones;
    private final CoordinacionRepositorio coordinaciones;
    private final LaboratorioRepositorio laboratorios;
    private final CensoDeBienes censoBienes;
    private final CensoDePersonal censoPersonal;
    private final ContextoUsuario contexto;

    public GestionOrganizacionServicio(DireccionRepositorio direcciones,
                                       CoordinacionRepositorio coordinaciones,
                                       LaboratorioRepositorio laboratorios,
                                       CensoDeBienes censoBienes,
                                       CensoDePersonal censoPersonal,
                                       ContextoUsuario contexto) {
        this.direcciones = direcciones;
        this.coordinaciones = coordinaciones;
        this.laboratorios = laboratorios;
        this.censoBienes = censoBienes;
        this.censoPersonal = censoPersonal;
        this.contexto = contexto;
    }

    /**
     * RNF-10: las consultas de lectura de la estructura tambien se aislan.
     *
     * <p>El resumen de una Coordinacion incluye cuantos bienes tiene y en que
     * condicion; entregarselo a un usuario de otra Coordinacion seria filtrar
     * su inventario en agregado. El Administrador si lee toda la
     * institucion (RF-46).</p>
     */
    private void exigirLecturaDe(Long coordinacionId) {
        if (!contexto.requerido().puedeLeerCoordinacion(coordinacionId)) {
            throw new AccesoDenegadoException(
                    "No tiene acceso a los datos de otra coordinacion.");
        }
    }

    // ------------------------------------------------------------------
    // Estructura completa (RF-15)
    // ------------------------------------------------------------------

    /**
     * Arbol institucional con el resumen de cada Coordinacion, resuelto en una
     * sola peticion para la pagina principal del Administrador.
     */
    @Transactional(readOnly = true)
    public EstructuraDto estructura(boolean soloActivas) {
        List<EstructuraDto.RamaDireccion> ramas = new ArrayList<>();
        for (Direccion direccion : direcciones.listar(soloActivas)) {
            List<CoordinacionDto> hijas = coordinaciones
                    .listarPorDireccion(direccion.getId(), soloActivas).stream()
                    .map(coordinacion -> componer(coordinacion, direccion.getNombre()))
                    .toList();
            ramas.add(new EstructuraDto.RamaDireccion(DireccionDto.de(direccion), hijas));
        }
        return EstructuraDto.de(ramas);
    }

    // ------------------------------------------------------------------
    // Direcciones (RF-10)
    //
    // Las direcciones de INICTEL-UNI son dos y estan en su reglamento: se
    // precargan con el esquema (migracion V4) y la aplicacion no las crea ni
    // las desactiva. Solo se leen y se corrigen sus datos.
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<DireccionDto> listarDirecciones(boolean soloActivas) {
        return direcciones.listar(soloActivas).stream().map(DireccionDto::de).toList();
    }

    @Transactional
    public DireccionDto editarDireccion(Long id, String nombre, String sigla, String descripcion) {
        Direccion direccion = exigirDireccion(id);
        validarNombreDireccionLibre(nombre, id);
        direccion.actualizar(nombre, sigla, descripcion);
        Direccion guardada = direcciones.guardar(direccion);

        return DireccionDto.de(guardada);
    }

    // ------------------------------------------------------------------
    // Coordinaciones (RF-11, RF-13)
    // ------------------------------------------------------------------

    /**
     * RF-49: el Administrador elige entre todas las Coordinaciones; un usuario
     * operativo solo se ve a si mismo, que es cuanto necesitan sus selectores.
     */
    @Transactional(readOnly = true)
    public List<CoordinacionDto> listarCoordinaciones(Long direccionId, boolean soloActivas) {
        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.esAdmin()) {
            return List.of(obtenerCoordinacion(actual.coordinacionRequerida()));
        }

        List<Coordinacion> encontradas = direccionId == null
                ? coordinaciones.listar(soloActivas)
                : coordinaciones.listarPorDireccion(direccionId, soloActivas);

        return encontradas.stream()
                .map(coordinacion -> componer(coordinacion, nombreDireccion(coordinacion.getDireccionId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CoordinacionDto obtenerCoordinacion(Long id) {
        exigirLecturaDe(id);
        Coordinacion coordinacion = exigirCoordinacion(id);
        return componer(coordinacion, nombreDireccion(coordinacion.getDireccionId()));
    }

    /**
     * RF-11, RN-26: alta de una Coordinacion junto a su primer Laboratorio.
     *
     * <p>Los dos nacen en la misma transaccion porque una Coordinacion sin
     * laboratorios no puede registrar bienes (RN-26); crearla "para completarla
     * despues" deja al Administrador con una unidad a medio hacer que nadie
     * puede usar. El nombre del primer laboratorio es obligatorio.</p>
     */
    @Transactional
    public CoordinacionDto crearCoordinacion(Long direccionId, String nombre, String descripcion,
                                             String laboratorioNombre, String laboratorioUbicacion) {
        Direccion direccion = exigirDireccion(direccionId);
        if (!direccion.isActiva()) {
            throw new DatosInvalidosException("direccionId",
                    "La direccion seleccionada esta desactivada.");
        }
        validarNombreCoordinacionLibre(direccionId, nombre, null);
        if (laboratorioNombre == null || laboratorioNombre.isBlank()) {
            throw new DatosInvalidosException("laboratorioNombre",
                    "Indique el nombre del primer laboratorio de la coordinacion.");
        }

        Coordinacion coordinacion = coordinaciones.guardar(
                Coordinacion.crear(direccionId, nombre, descripcion));

        laboratorios.guardar(Laboratorio.crear(
                coordinacion.getId(), laboratorioNombre, laboratorioUbicacion));

        return componer(coordinacion, direccion.getNombre());
    }

    @Transactional
    public CoordinacionDto editarCoordinacion(Long id, String nombre, String descripcion) {
        Coordinacion coordinacion = exigirCoordinacion(id);
        validarNombreCoordinacionLibre(coordinacion.getDireccionId(), nombre, id);
        coordinacion.actualizar(nombre, descripcion);
        Coordinacion guardada = coordinaciones.guardar(coordinacion);

        return componer(guardada, nombreDireccion(guardada.getDireccionId()));
    }

    /** RF-13: la desactivacion se bloquea si quedan bienes o prestamos vivos. */
    @Transactional
    public CoordinacionDto cambiarEstadoCoordinacion(Long id, boolean activa) {
        Coordinacion coordinacion = exigirCoordinacion(id);
        if (activa) {
            coordinacion.activar();
        } else {
            coordinacion.desactivar(
                    censoBienes.bienesActivosEn(id),
                    censoBienes.prestamosVigentesEn(id));
        }
        Coordinacion guardada = coordinaciones.guardar(coordinacion);

        return componer(guardada, nombreDireccion(guardada.getDireccionId()));
    }

    // ------------------------------------------------------------------
    // Laboratorios (RF-12, RF-14)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<LaboratorioDto> listarLaboratorios(Long coordinacionId, boolean soloActivos) {
        exigirLecturaDe(coordinacionId);
        exigirCoordinacion(coordinacionId);
        return laboratorios.listarPorCoordinacion(coordinacionId, soloActivos).stream()
                .map(laboratorio -> LaboratorioDto.de(laboratorio,
                        censoBienes.bienesUbicadosEn(laboratorio.getId())))
                .toList();
    }

    @Transactional
    public LaboratorioDto crearLaboratorio(Long coordinacionId, String nombre, String ubicacion) {
        Coordinacion coordinacion = exigirCoordinacion(coordinacionId);
        if (!coordinacion.isActiva()) {
            throw new DatosInvalidosException("coordinacionId",
                    "La coordinacion seleccionada esta desactivada.");
        }
        validarNombreLaboratorioLibre(coordinacionId, nombre, null);

        Laboratorio laboratorio = laboratorios.guardar(
                Laboratorio.crear(coordinacionId, nombre, ubicacion));

        return LaboratorioDto.de(laboratorio, 0);
    }

    @Transactional
    public LaboratorioDto editarLaboratorio(Long id, String nombre, String ubicacion) {
        Laboratorio laboratorio = exigirLaboratorio(id);
        validarNombreLaboratorioLibre(laboratorio.getCoordinacionId(), nombre, id);
        laboratorio.actualizar(nombre, ubicacion);
        Laboratorio guardado = laboratorios.guardar(laboratorio);

        return LaboratorioDto.de(guardado, censoBienes.bienesUbicadosEn(guardado.getId()));
    }

    @Transactional
    public LaboratorioDto cambiarEstadoLaboratorio(Long id, boolean activo) {
        Laboratorio laboratorio = exigirLaboratorio(id);
        if (activo) {
            laboratorio.activar();
        } else {
            // RN-26: la coordinacion nunca se queda sin laboratorios activos.
            boolean esElUltimo = laboratorio.isActivo()
                    && laboratorios.contarActivosPorCoordinacion(laboratorio.getCoordinacionId()) <= 1;
            laboratorio.desactivar(censoBienes.bienesUbicadosEn(id), esElUltimo);
        }
        Laboratorio guardado = laboratorios.guardar(laboratorio);

        return LaboratorioDto.de(guardado, censoBienes.bienesUbicadosEn(guardado.getId()));
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private CoordinacionDto componer(Coordinacion coordinacion, String direccionNombre) {
        Long id = coordinacion.getId();
        return CoordinacionDto.de(
                coordinacion,
                direccionNombre,
                censoPersonal.responsableDe(id).orElse(null),
                censoPersonal.operadoresActivosEn(id),
                laboratorios.contarActivosPorCoordinacion(id),
                censoBienes.resumenDe(id));
    }

    private String nombreDireccion(Long direccionId) {
        return direcciones.buscarPorId(direccionId).map(Direccion::getNombre).orElse("Direccion no disponible");
    }

    private Direccion exigirDireccion(Long id) {
        return direcciones.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la direccion", id));
    }

    private Coordinacion exigirCoordinacion(Long id) {
        return coordinaciones.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la coordinacion", id));
    }

    private Laboratorio exigirLaboratorio(Long id) {
        return laboratorios.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el laboratorio", id));
    }

    private void validarNombreDireccionLibre(String nombre, Long idActual) {
        if (nombre != null && direcciones.existeNombre(nombre.trim(), idActual)) {
            throw new DatosInvalidosException("nombre", "Ya existe una direccion con ese nombre.");
        }
    }

    private void validarNombreCoordinacionLibre(Long direccionId, String nombre, Long idActual) {
        if (nombre != null && coordinaciones.existeNombreEnDireccion(direccionId, nombre.trim(), idActual)) {
            throw new DatosInvalidosException("nombre",
                    "Ya existe una coordinacion con ese nombre en la misma direccion.");
        }
    }

    private void validarNombreLaboratorioLibre(Long coordinacionId, String nombre, Long idActual) {
        if (nombre != null && laboratorios.existeNombreEnCoordinacion(coordinacionId, nombre.trim(), idActual)) {
            throw new DatosInvalidosException("nombre",
                    "Ya existe un laboratorio con ese nombre en la misma coordinacion.");
        }
    }
}
