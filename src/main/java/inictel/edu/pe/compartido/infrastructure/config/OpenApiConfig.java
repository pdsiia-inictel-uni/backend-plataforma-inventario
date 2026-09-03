package inictel.edu.pe.compartido.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion de la API con OpenAPI/Swagger (RNF-17).
 * Disponible en /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI apiInventario() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Sistema de Gestion de Inventarios INICTEL-UNI")
                        .version("3.0")
                        .description("""
                                API REST del Sistema de Gestion de Inventarios de INICTEL-UNI, organizada por contextos delimitados (DDD):

                                  /api/iam         identidad y acceso (usuarios y autenticacion)
                                  /api/inventario  categorias y bienes patrimoniales
                                  /api/prestamos   salidas y devoluciones de bienes
                                  /api/reportes    panel de control y exportaciones

                                Todos los endpoints, salvo /api/iam/auth/login, requieren un token JWT
                                valido en la cabecera Authorization (esquema Bearer).
                                """)
                        .contact(new Contact()
                                .name("Area de Investigacion - INICTEL-UNI")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .name(ESQUEMA_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token obtenido en POST /api/iam/auth/login")));
    }
}
