package inictel.edu.pe.iam.infrastructure.inicializacion;

import inictel.edu.pe.compartido.infrastructure.config.AppProperties;
import inictel.edu.pe.iam.domain.model.CorreoInstitucional;
import inictel.edu.pe.iam.domain.model.Dni;
import inictel.edu.pe.iam.domain.model.NombrePersona;
import inictel.edu.pe.iam.domain.model.NombreUsuario;
import inictel.edu.pe.iam.domain.model.Usuario;
import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import inictel.edu.pe.iam.domain.service.CifradorPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea la cuenta administradora inicial cuando la tabla de usuarios esta vacia.
 * El sistema exige cambiar la contrasena en el primer ingreso (RF-06).
 */
@Component
public class CuentaInicialInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CuentaInicialInitializer.class);

    private final UsuarioRepositorio usuarios;
    private final CifradorPassword cifrador;
    private final AppProperties propiedades;

    public CuentaInicialInitializer(UsuarioRepositorio usuarios,
                                    CifradorPassword cifrador,
                                    AppProperties propiedades) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.propiedades = propiedades;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.CuentaInicial cfg = propiedades.cuentaInicial();
        if (!cfg.habilitada() || !usuarios.estaVacio()) {
            return;
        }

        // Es el unico usuario que nace con rol y con contrasena: no hay
        // todavia nadie que pueda asignarselos (RF-16b, RN-05).
        Usuario admin = Usuario.registrarAdministradorInicial(
                new NombreUsuario(cfg.username()),
                new NombrePersona("Administrador", "del", "Sistema"),
                new Dni(cfg.dni()),
                new CorreoInstitucional(cfg.correo()),
                cifrador.cifrar(cfg.password()));

        usuarios.guardar(admin);

        log.warn("""
                ================================================================
                 CUENTA ADMINISTRADORA INICIAL CREADA
                 Usuario    : {}
                 Contrasena : {}

                 Los datos personales de esta cuenta son de relleno. En el primer
                 ingreso el sistema pedira los reales (nombres, apellidos y DNI)
                 junto con la contrasena definitiva (RF-06b).

                 PRIMER PASO: cree sus coordinaciones desde 'Direcciones'.
                 Las dos Direcciones de la institucion vienen dadas (RN-30) y cada
                 coordinacion nace con su primer laboratorio.

                 SEGUNDO PASO: en 'Personas', registre a la gente y despues
                 asignele su puesto. Son dos actos: el registro dice quien es la
                 persona; la asignacion, que hace y donde, y es cuando nace su
                 contrasena. Una coordinacion no puede operar hasta tener su
                 responsable.
                ================================================================
                """, cfg.username(), cfg.password());
    }
}
