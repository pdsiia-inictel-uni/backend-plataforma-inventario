package inictel.edu.pe;

import inictel.edu.pe.compartido.infrastructure.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Sistema de Gestion de Inventarios - INICTEL-UNI, Area de Investigacion.
 *
 * <p>Monolito modular organizado por contextos delimitados (DDD). Cada contexto
 * —{@code iam}, {@code inventario}, {@code prestamos}, {@code auditoria} y
 * {@code reportes}— se estructura en cuatro capas: dominio, aplicacion,
 * infraestructura y presentacion (RNF-14).</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class InventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }
}
