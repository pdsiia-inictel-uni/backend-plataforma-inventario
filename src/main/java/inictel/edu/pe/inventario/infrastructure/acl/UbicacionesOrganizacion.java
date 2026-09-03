package inictel.edu.pe.inventario.infrastructure.acl;

import inictel.edu.pe.inventario.domain.service.UbicacionesDisponibles;
import inictel.edu.pe.organizacion.application.service.ServicioPublicoOrganizacion;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador anticorrupcion de {@code inventario} hacia {@code organizacion}
 * (RNF-39).
 */
@Component
public class UbicacionesOrganizacion implements UbicacionesDisponibles {

    private final ServicioPublicoOrganizacion organizacion;

    public UbicacionesOrganizacion(ServicioPublicoOrganizacion organizacion) {
        this.organizacion = organizacion;
    }

    @Override
    public boolean laboratorioPerteneceA(Long laboratorioId, Long coordinacionId) {
        return laboratorioId != null && coordinacionId != null
                && organizacion.laboratorioPerteneceA(laboratorioId, coordinacionId);
    }

    @Override
    public boolean tieneLaboratoriosActivos(Long coordinacionId) {
        return organizacion.tieneLaboratoriosActivos(coordinacionId);
    }

    @Override
    public Optional<String> nombreDeLaboratorio(Long laboratorioId) {
        return laboratorioId == null ? Optional.empty()
                : organizacion.nombreDeLaboratorio(laboratorioId);
    }

    @Override
    public Optional<String> nombreDeCoordinacion(Long coordinacionId) {
        return coordinacionId == null ? Optional.empty()
                : organizacion.nombreDeCoordinacion(coordinacionId);
    }
}
