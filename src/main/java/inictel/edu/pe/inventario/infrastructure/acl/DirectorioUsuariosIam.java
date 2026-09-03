package inictel.edu.pe.inventario.infrastructure.acl;

import inictel.edu.pe.iam.application.service.ServicioPublicoIam;
import inictel.edu.pe.inventario.domain.service.DirectorioUsuarios;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador anticorrupcion de {@code inventario} hacia {@code iam} (RNF-39).
 */
@Component
public class DirectorioUsuariosIam implements DirectorioUsuarios {

    private final ServicioPublicoIam iam;

    public DirectorioUsuariosIam(ServicioPublicoIam iam) {
        this.iam = iam;
    }

    @Override
    public Optional<String> nombreDe(Long usuarioId) {
        return iam.nombreDe(usuarioId);
    }

    @Override
    public Optional<Long> responsableVigenteDe(Long coordinacionId) {
        if (coordinacionId == null) {
            return Optional.empty();
        }
        return iam.responsableDe(coordinacionId).map(ServicioPublicoIam.ResponsableDto::id);
    }

    @Override
    public boolean esOperadorActivoDe(Long usuarioId, Long coordinacionId) {
        return iam.esOperadorActivoDe(usuarioId, coordinacionId);
    }
}
