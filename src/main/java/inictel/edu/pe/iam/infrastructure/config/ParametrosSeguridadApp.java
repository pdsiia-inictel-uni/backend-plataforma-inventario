package inictel.edu.pe.iam.infrastructure.config;

import inictel.edu.pe.compartido.infrastructure.config.AppProperties;
import inictel.edu.pe.iam.application.port.ParametrosSeguridad;
import org.springframework.stereotype.Component;

/**
 * Adaptador que expone la configuracion externalizada como el puerto
 * {@link ParametrosSeguridad} que consume la capa de aplicacion (RNF-19).
 */
@Component
public class ParametrosSeguridadApp implements ParametrosSeguridad {

    private final AppProperties propiedades;

    public ParametrosSeguridadApp(AppProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public int maxIntentosFallidos() {
        return propiedades.seguridad().maxIntentosFallidos();
    }

    @Override
    public int minutosBloqueo() {
        return propiedades.seguridad().minutosBloqueo();
    }
}
