package inictel.edu.pe.compartido.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Ajustes de Spring MVC comunes a todos los contextos.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Permite recibir enums en minusculas desde los parametros de consulta
        // (por ejemplo ?estado=disponible).
        registry.addConverterFactory(new ConvertidorEnumInsensible());
    }
}
