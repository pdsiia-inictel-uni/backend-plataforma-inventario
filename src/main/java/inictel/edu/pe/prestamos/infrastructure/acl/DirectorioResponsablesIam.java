package inictel.edu.pe.prestamos.infrastructure.acl;

import inictel.edu.pe.iam.application.service.ServicioPublicoIam;
import inictel.edu.pe.prestamos.domain.model.Destinatario;
import inictel.edu.pe.prestamos.domain.service.DirectorioResponsables;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador anticorrupcion de {@code prestamos} hacia {@code iam} (RNF-39).
 *
 * <p>Traduce la unica pregunta que los prestamos le hacen a las personas
 * —"tiene esta coordinacion responsable vigente?"— al servicio publico del
 * otro contexto, sin conocer el agregado Usuario.</p>
 */
@Component
public class DirectorioResponsablesIam implements DirectorioResponsables {

    private final ServicioPublicoIam iam;

    public DirectorioResponsablesIam(ServicioPublicoIam iam) {
        this.iam = iam;
    }

    @Override
    public boolean tieneResponsableVigente(Long coordinacionId) {
        return coordinacionId != null && iam.responsableDe(coordinacionId).isPresent();
    }

    @Override
    public List<Destinatario> destinatariosDe(Long coordinacionId) {
        return iam.personalOperativoDe(coordinacionId).stream().map(this::traducir).toList();
    }

    @Override
    public Optional<Destinatario> destinatario(Long usuarioId) {
        return iam.personalOperativo(usuarioId).map(this::traducir);
    }

    private Destinatario traducir(ServicioPublicoIam.PersonaOperativaDto persona) {
        return new Destinatario(persona.id(), persona.nombreCompleto(), persona.dni(),
                persona.rolEtiqueta(), persona.coordinacionId());
    }
}
