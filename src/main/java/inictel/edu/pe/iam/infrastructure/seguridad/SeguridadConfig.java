package inictel.edu.pe.iam.infrastructure.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import inictel.edu.pe.compartido.infrastructure.config.AppProperties;
import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion de seguridad del sistema (RNF-02, RNF-04).
 *
 * <p>Vive en el contexto {@code iam} porque la identidad y el control de
 * acceso son su responsabilidad; los demas contextos solo declaran sus reglas
 * de autorizacion con {@code @PreAuthorize}.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SeguridadConfig {

    /**
     * Content-Security-Policy de la API (RNF-07).
     *
     * <p>Este servidor no entrega ninguna pagina: responde JSON, y las
     * fotografias y los PDF de baja que el frontend descarga con el token JWT
     * y muestra como blob desde su propio origen (la CSP que rige esos blobs es
     * la del frontend, no esta). Por eso la politica es la de una API: nada
     * puede cargarse, incrustarse ni enviarse desde una respuesta suya. Si
     * alguien abriera una respuesta en el navegador, no ejecutaria nada.</p>
     *
     * <p>La politica de la aplicacion —la que permite el bundle, los estilos,
     * las fotos en blob y los PDF en marco— la emite quien sirve el HTML: el
     * nginx del frontend (seguridad-cabeceras.conf) y, en desarrollo, la
     * etiqueta meta de index.html.</p>
     */
    private static final String CSP_API = String.join("; ",
            "default-src 'none'",
            "frame-ancestors 'none'",
            "base-uri 'none'",
            "form-action 'none'");

    /** Rutas de la documentacion OpenAPI (RNF-17). */
    private static final String[] RUTAS_DOCUMENTACION = {
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    private static final String[] RUTAS_PUBLICAS = {
            "/api/auth/login",
            "/actuator/health"
    };

    private final AppProperties propiedades;
    private final ServicioJwt servicioJwt;
    private final UsuarioRepositorio usuarios;
    private final ObjectMapper objectMapper;
    private final ManejadorNoAutenticado manejadorNoAutenticado;
    private final ManejadorAccesoDenegado manejadorAccesoDenegado;

    public SeguridadConfig(AppProperties propiedades,
                           ServicioJwt servicioJwt,
                           UsuarioRepositorio usuarios,
                           ObjectMapper objectMapper,
                           ManejadorNoAutenticado manejadorNoAutenticado,
                           ManejadorAccesoDenegado manejadorAccesoDenegado) {
        this.propiedades = propiedades;
        this.servicioJwt = servicioJwt;
        this.usuarios = usuarios;
        this.objectMapper = objectMapper;
        this.manejadorNoAutenticado = manejadorNoAutenticado;
        this.manejadorAccesoDenegado = manejadorAccesoDenegado;
    }

    /** RNF-03: BCrypt con factor de costo 10. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Cadena exclusiva de Swagger UI, que necesita estilos en linea propios.
     * Se aisla para que la politica estricta de la aplicacion no tenga que
     * relajarse; en produccion puede desactivarse con
     * {@code springdoc.api-docs.enabled=false}.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain cadenaDocumentacion(HttpSecurity http) throws Exception {
        http
                .securityMatcher(RUTAS_DOCUMENTACION)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(peticiones -> peticiones.anyRequest().permitAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(String.join("; ",
                                "default-src 'self'",
                                "script-src 'self' 'unsafe-inline'",
                                "style-src 'self' 'unsafe-inline'",
                                "img-src 'self' data:",
                                "object-src 'none'",
                                "frame-ancestors 'none'")))
                        .frameOptions(frame -> frame.deny()));
        return http.build();
    }

    /** Cadena principal de la API y de los recursos de la aplicacion. */
    @Bean
    @Order(2)
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter filtroJwt = new JwtAuthenticationFilter(servicioJwt, usuarios);
        ForzarCambioPasswordFilter filtroCambioPassword = new ForzarCambioPasswordFilter(objectMapper);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(configuracionCors()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(manejo -> manejo
                        .authenticationEntryPoint(manejadorNoAutenticado)
                        .accessDeniedHandler(manejadorAccesoDenegado))
                .headers(headers -> headers
                        // RNF-04: cabeceras de endurecimiento del navegador.
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CSP_API))
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                        // La camara se habilita solo para el propio origen: es lo que permite
                        // fotografiar el bien desde la tablet al registrarlo (RF-51b). El resto
                        // de capacidades sigue denegado por completo, y 'self' no autoriza a
                        // ningun tercero incrustado, porque frame-ancestors 'none' y
                        // X-Frame-Options DENY impiden que la aplicacion viaje en un marco.
                        .permissionsPolicyHeader(permisos -> permisos.policy(
                                "camera=(self), microphone=(), geolocation=(), payment=(), usb=(), interest-cohort=()"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)))
                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(RUTAS_PUBLICAS).permitAll()
                        // Estructura organizacional: solo el Administrador escribe (RF-10 .. RF-12).
                        // La lectura queda abierta porque los formularios de inventario
                        // necesitan poblar sus selectores de coordinacion y laboratorio.
                        .requestMatchers(HttpMethod.POST, "/api/organizacion/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/organizacion/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/organizacion/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/organizacion/**").denyAll()
                        // Catalogo de categorias: escritura solo del Administrador (RF-31)
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/categorias/**").hasRole("ADMIN")
                        // RF-28d: la baja de un PUESTO es el unico DELETE que admite
                        // /api/usuarios, y no borra a nadie: retira la asignacion y deja
                        // la cuenta activa. Va antes de la denegacion de abajo porque las
                        // reglas se evaluan en orden y '/api/usuarios/**' la alcanzaria.
                        // El rol y el ambito los comprueban @PreAuthorize y el servicio.
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/*/asignaciones/*").authenticated()
                        // RN-09: los usuarios nunca se eliminan, solo se dan de baja.
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").denyAll()
                        // RN-21: el historial de un bien no admite escritura por ningun medio.
                        .requestMatchers(HttpMethod.DELETE, "/api/equipos/**").denyAll()
                        // El resto exige token valido; el detalle por rol y por coordinacion
                        // se aplica con @PreAuthorize y en la capa de aplicacion.
                        .anyRequest().authenticated())
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(filtroCambioPassword, JwtAuthenticationFilter.class);

        return http.build();
    }

    /** RNF-11: el frontend Angular se sirve desde otro origen en desarrollo. */
    @Bean
    public CorsConfigurationSource configuracionCors() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(propiedades.cors().origenesPermitidos());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
