package inictel.edu.pe.organizacion.infrastructure.acl;

import inictel.edu.pe.iam.application.service.ServicioPublicoIam;
import inictel.edu.pe.organizacion.domain.service.CensoDePersonal;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador anticorrupcion de {@code organizacion} hacia {@code iam}.
 *
 * <p>Traduce el vocabulario de identidad al que necesita la vista de
 * estructura: quien esta a cargo y cuanta gente hay (RF-15).</p>
 */
@Component
public class CensoDePersonalIam implements CensoDePersonal {

    private final ServicioPublicoIam iam;

    public CensoDePersonalIam(ServicioPublicoIam iam) {
        this.iam = iam;
    }

    @Override
    public Optional<ResponsableVigente> responsableDe(Long coordinacionId) {
        if (coordinacionId == null) {
            return Optional.empty();
        }
        return iam.responsableDe(coordinacionId)
                .map(r -> new ResponsableVigente(r.id(), r.nombreCompleto(), r.dni()));
    }

    @Override
    public long operadoresActivosEn(Long coordinacionId) {
        return iam.contarOperadoresActivosEn(coordinacionId);
    }
}
