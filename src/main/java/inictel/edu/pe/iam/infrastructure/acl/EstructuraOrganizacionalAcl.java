package inictel.edu.pe.iam.infrastructure.acl;

import inictel.edu.pe.iam.domain.service.EstructuraOrganizacional;
import inictel.edu.pe.organizacion.application.service.ServicioPublicoOrganizacion;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador anticorrupcion de {@code iam} hacia {@code organizacion}.
 *
 * <p>Traduce la necesidad de iam ("existe esta coordinacion?") al servicio
 * publico del otro contexto, sin que iam conozca sus agregados ni sus tablas
 * (RNF-39).</p>
 */
@Component
public class EstructuraOrganizacionalAcl implements EstructuraOrganizacional {

    private final ServicioPublicoOrganizacion organizacion;

    public EstructuraOrganizacionalAcl(ServicioPublicoOrganizacion organizacion) {
        this.organizacion = organizacion;
    }

    @Override
    public boolean existeAlgunaCoordinacion() {
        return organizacion.existeAlgunaCoordinacion();
    }

    @Override
    public boolean existeCoordinacionActiva(Long coordinacionId) {
        return coordinacionId != null && organizacion.existeCoordinacionActiva(coordinacionId);
    }

    @Override
    public Optional<Ubicacion> ubicacionDeCoordinacion(Long coordinacionId) {
        return organizacion.ubicacionDeCoordinacion(coordinacionId)
                .map(ubicada -> new Ubicacion(ubicada.nombre(), ubicada.direccion()));
    }
}
