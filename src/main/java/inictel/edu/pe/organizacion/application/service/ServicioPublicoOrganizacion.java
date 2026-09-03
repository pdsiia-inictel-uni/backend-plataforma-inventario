package inictel.edu.pe.organizacion.application.service;

import inictel.edu.pe.organizacion.domain.model.Coordinacion;
import inictel.edu.pe.organizacion.domain.model.Laboratorio;
import inictel.edu.pe.organizacion.domain.repository.CoordinacionRepositorio;
import inictel.edu.pe.organizacion.domain.repository.DireccionRepositorio;
import inictel.edu.pe.organizacion.domain.repository.LaboratorioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cara publica del contexto {@code organizacion} hacia los demas contextos.
 *
 * <p>Los adaptadores anticorrupcion de {@code iam}, {@code inventario} y
 * {@code prestamos} consumen este servicio, nunca los repositorios ni las
 * entidades JPA de este contexto (RNF-39).</p>
 */
@Service
public class ServicioPublicoOrganizacion {

    private final DireccionRepositorio direcciones;
    private final CoordinacionRepositorio coordinaciones;
    private final LaboratorioRepositorio laboratorios;

    public ServicioPublicoOrganizacion(DireccionRepositorio direcciones,
                                       CoordinacionRepositorio coordinaciones,
                                       LaboratorioRepositorio laboratorios) {
        this.direcciones = direcciones;
        this.coordinaciones = coordinaciones;
        this.laboratorios = laboratorios;
    }

    /** RF-17: gobierna si pueden crearse usuarios operativos. */
    @Transactional(readOnly = true)
    public boolean existeAlgunaCoordinacion() {
        return coordinaciones.existeAlguna();
    }

    @Transactional(readOnly = true)
    public boolean existeCoordinacionActiva(Long coordinacionId) {
        return coordinaciones.buscarPorId(coordinacionId).map(Coordinacion::isActiva).orElse(false);
    }

    /** Nombre completo "Coordinacion X - Direccion Y", para encabezados y bitacora. */
    @Transactional(readOnly = true)
    public Optional<String> nombreCompletoDeCoordinacion(Long coordinacionId) {
        return coordinaciones.buscarPorId(coordinacionId)
                .map(coordinacion -> {
                    String direccion = direcciones.buscarPorId(coordinacion.getDireccionId())
                            .map(d -> d.getNombre())
                            .orElse("Direccion no disponible");
                    return coordinacion.getNombre() + " - " + direccion;
                });
    }

    @Transactional(readOnly = true)
    public Optional<String> nombreDeCoordinacion(Long coordinacionId) {
        return coordinaciones.buscarPorId(coordinacionId).map(Coordinacion::getNombre);
    }

    /**
     * Coordinacion y Direccion a la que pertenece, en una sola consulta.
     *
     * <p>Lo consume {@code iam} para decirle a cada persona donde trabaja: en
     * la ficha, en el encabezado y en el saludo de bienvenida (RF-01b). Van
     * juntas porque juntas se muestran, y pedirlas por separado seria consultar
     * dos veces lo mismo.</p>
     */
    @Transactional(readOnly = true)
    public Optional<CoordinacionUbicada> ubicacionDeCoordinacion(Long coordinacionId) {
        if (coordinacionId == null) {
            return Optional.empty();
        }
        return coordinaciones.buscarPorId(coordinacionId)
                .map(coordinacion -> new CoordinacionUbicada(
                        coordinacion.getNombre(),
                        direcciones.buscarPorId(coordinacion.getDireccionId())
                                .map(direccion -> direccion.getNombre())
                                .orElse(null)));
    }

    /** Nombre de una Coordinacion junto al de su Direccion. */
    public record CoordinacionUbicada(String nombre, String direccion) {
    }

    /**
     * RN-12: valida que el laboratorio exista, este activo y pertenezca a la
     * Coordinacion indicada. Lo consume {@code inventario} al ubicar un bien.
     */
    @Transactional(readOnly = true)
    public boolean laboratorioPerteneceA(Long laboratorioId, Long coordinacionId) {
        return laboratorios.buscarPorId(laboratorioId)
                .filter(Laboratorio::isActivo)
                .map(laboratorio -> laboratorio.perteneceA(coordinacionId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Optional<String> nombreDeLaboratorio(Long laboratorioId) {
        return laboratorios.buscarPorId(laboratorioId).map(Laboratorio::getNombre);
    }

    /**
     * RN-26: gobierna si una Coordinacion puede registrar bienes.
     *
     * <p>Lo consume {@code inventario} antes de dar de alta un bien: un bien
     * sin ningun lugar donde estar no se puede encontrar despues.</p>
     */
    @Transactional(readOnly = true)
    public boolean tieneLaboratoriosActivos(Long coordinacionId) {
        return coordinacionId != null
                && laboratorios.contarActivosPorCoordinacion(coordinacionId) > 0;
    }
}
