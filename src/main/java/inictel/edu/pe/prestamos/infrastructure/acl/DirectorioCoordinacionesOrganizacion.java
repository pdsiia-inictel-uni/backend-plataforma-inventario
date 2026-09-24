package inictel.edu.pe.prestamos.infrastructure.acl;

import inictel.edu.pe.organizacion.application.service.ServicioPublicoOrganizacion;
import inictel.edu.pe.prestamos.domain.model.CoordinacionDestino;
import inictel.edu.pe.prestamos.domain.service.DirectorioCoordinaciones;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador anticorrupcion de {@code prestamos} hacia {@code organizacion} (RNF-39). */
@Component
public class DirectorioCoordinacionesOrganizacion implements DirectorioCoordinaciones {

    private final ServicioPublicoOrganizacion organizacion;

    public DirectorioCoordinacionesOrganizacion(ServicioPublicoOrganizacion organizacion) {
        this.organizacion = organizacion;
    }

    @Override
    public Optional<String> nombreDe(Long coordinacionId) {
        return coordinacionId == null ? Optional.empty() : organizacion.nombreDeCoordinacion(coordinacionId);
    }

    @Override
    public List<CoordinacionDestino> todas() {
        return organizacion.listarCoordinacionesConDireccion().stream()
                .map(c -> new CoordinacionDestino(c.id(), c.nombre(), c.direccionId(), c.direccionNombre()))
                .toList();
    }
}
