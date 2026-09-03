package inictel.edu.pe.iam.application.service;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.iam.application.comando.AutenticarComando;
import inictel.edu.pe.iam.application.comando.CambiarCredencialesComando;
import inictel.edu.pe.iam.application.comando.CambiarPasswordComando;
import inictel.edu.pe.iam.application.comando.CompletarPrimerIngresoComando;
import inictel.edu.pe.iam.application.dto.SesionDto;
import inictel.edu.pe.iam.application.dto.UsuarioDto;
import inictel.edu.pe.iam.application.port.GeneradorTokens;
import inictel.edu.pe.iam.application.port.ParametrosSeguridad;
import inictel.edu.pe.iam.domain.model.CorreoInstitucional;
import inictel.edu.pe.iam.domain.model.Dni;
import inictel.edu.pe.iam.domain.model.NombrePersona;
import inictel.edu.pe.iam.domain.model.NombreUsuario;
import inictel.edu.pe.iam.domain.model.Usuario;
import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import inictel.edu.pe.iam.domain.service.CifradorPassword;
import inictel.edu.pe.iam.domain.service.EstructuraOrganizacional;
import inictel.edu.pe.iam.domain.service.PoliticaPassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Casos de uso de acceso al sistema (RF-01 .. RF-06).
 *
 * <p>Orquesta el agregado Usuario y los puertos de cifrado y emision de
 * tokens; no contiene reglas de negocio propias, esas viven en el dominio.</p>
 */
@Service
public class AutenticacionServicio {

    private static final Logger log = LoggerFactory.getLogger(AutenticacionServicio.class);
    private static final String MENSAJE_CREDENCIALES = "Usuario o contrasena incorrectos.";

    /** RF-07: mensaje literal exigido por la especificacion. */
    private static final String MENSAJE_CUENTA_INACTIVA =
            "Su cuenta se encuentra inactiva. Comuniquese con el administrador del sistema.";

    private final UsuarioRepositorio usuarios;
    private final CifradorPassword cifrador;
    private final GeneradorTokens generadorTokens;
    private final ParametrosSeguridad parametros;
    private final EstructuraOrganizacional estructura;

    public AutenticacionServicio(UsuarioRepositorio usuarios,
                                 CifradorPassword cifrador,
                                 GeneradorTokens generadorTokens,
                                 ParametrosSeguridad parametros,
                                 EstructuraOrganizacional estructura) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.generadorTokens = generadorTokens;
        this.parametros = parametros;
        this.estructura = estructura;
    }

    /** RF-01, RF-02, RNF-06: verifica credenciales y emite el token de acceso. */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public SesionDto iniciarSesion(AutenticarComando comando) {
        Optional<Usuario> encontrado = usuarios.buscarPorUsernameOCorreo(comando.identificador().trim());

        if (encontrado.isEmpty()) {
            throw new BadCredentialsException(MENSAJE_CREDENCIALES);
        }

        Usuario usuario = encontrado.get();

        // RF-07: una cuenta inactiva no autentica y no revela nada mas.
        if (!usuario.estaActiva()) {
            throw new BadCredentialsException(MENSAJE_CUENTA_INACTIVA);
        }

        if (usuario.estaBloqueado()) {
            throw new BadCredentialsException("Cuenta bloqueada temporalmente por intentos fallidos. Intente nuevamente en "
                    + usuario.minutosDeBloqueoRestantes() + " minuto(s).");
        }

        if (!cifrador.coincide(comando.password(), usuario.getPasswordHash())) {
            registrarIntentoFallido(usuario);
            throw new BadCredentialsException(MENSAJE_CREDENCIALES);
        }

        usuario.registrarAccesoExitoso();
        usuarios.guardar(usuario);

        return construirSesion(usuario);
    }

    /** RNF-06: contabiliza el intento y bloquea la cuenta al alcanzar el maximo. */
    private void registrarIntentoFallido(Usuario usuario) {
        int maximo = parametros.maxIntentosFallidos();
        int minutos = parametros.minutosBloqueo();
        boolean bloqueada = usuario.registrarIntentoFallido(maximo, minutos);
        usuarios.guardar(usuario);

        if (bloqueada) {
            log.warn("Cuenta '{}' bloqueada temporalmente por intentos fallidos.", usuario.getUsername().valor());
        }
    }

    @Transactional(readOnly = true)
    public UsuarioDto perfil(Long usuarioId) {
        return componer(exigirUsuario(usuarioId));
    }

    /** RF-06, RNF-05: cambio de la propia contrasena; devuelve una sesion renovada. */
    @Transactional
    public SesionDto cambiarPasswordPropia(CambiarPasswordComando comando) {
        Usuario usuario = exigirUsuario(comando.usuarioId());

        if (!cifrador.coincide(comando.passwordActual(), usuario.getPasswordHash())) {
            throw new DatosInvalidosException("passwordActual", "La contrasena actual no es correcta.");
        }
        if (!comando.passwordNueva().equals(comando.confirmacion())) {
            throw new DatosInvalidosException("confirmacion", "La confirmacion no coincide con la nueva contrasena.");
        }
        if (!PoliticaPassword.esValida(comando.passwordNueva())) {
            throw new DatosInvalidosException("passwordNueva", PoliticaPassword.MENSAJE);
        }
        if (cifrador.coincide(comando.passwordNueva(), usuario.getPasswordHash())) {
            throw new DatosInvalidosException("passwordNueva", "La nueva contrasena debe ser distinta de la actual.");
        }

        usuario.cambiarPassword(cifrador.cifrar(comando.passwordNueva()));
        usuarios.guardar(usuario);

        return construirSesion(usuario);
    }

    /**
     * RF-06b: primer ingreso completo. En una sola transaccion el usuario
     * confirma su identidad real y define su contrasena definitiva.
     *
     * <p>Existe porque la cuenta administradora inicial nace con datos de
     * relleno: sin este paso, el sistema arrancaria identificando al
     * Administrador con un nombre y un DNI que no son suyos.</p>
     */
    @Transactional
    public SesionDto completarPrimerIngreso(CompletarPrimerIngresoComando comando) {
        Usuario usuario = exigirUsuario(comando.usuarioId());

        if (!cifrador.coincide(comando.passwordActual(), usuario.getPasswordHash())) {
            throw new DatosInvalidosException("passwordActual", "La contrasena actual no es correcta.");
        }
        if (!comando.passwordNueva().equals(comando.confirmacion())) {
            throw new DatosInvalidosException("confirmacion", "La confirmacion no coincide con la nueva contrasena.");
        }
        if (!PoliticaPassword.esValida(comando.passwordNueva())) {
            throw new DatosInvalidosException("passwordNueva", PoliticaPassword.MENSAJE);
        }
        if (cifrador.coincide(comando.passwordNueva(), usuario.getPasswordHash())) {
            throw new DatosInvalidosException("passwordNueva", "La nueva contrasena debe ser distinta de la actual.");
        }

        NombrePersona nombre = new NombrePersona(
                comando.nombres(), comando.primerApellido(), comando.segundoApellido());
        Dni dni = new Dni(comando.dni());

        // RF-24: el DNI identifica a la persona en todo el sistema.
        if (usuarios.existeDni(dni.valor(), usuario.getId())) {
            throw new DatosInvalidosException("dni", "Ya existe otra persona registrada con ese DNI.");
        }

        usuario.completarPrimerIngreso(nombre, dni, cifrador.cifrar(comando.passwordNueva()));
        usuarios.guardar(usuario);

        return construirSesion(usuario);
    }

    /** Cambio del propio nombre de usuario y correo, confirmado con la contrasena vigente. */
    @Transactional
    public SesionDto cambiarCredencialesPropias(CambiarCredencialesComando comando) {
        Usuario usuario = exigirUsuario(comando.usuarioId());

        if (!cifrador.coincide(comando.passwordActual(), usuario.getPasswordHash())) {
            throw new DatosInvalidosException("passwordActual", "La contrasena actual no es correcta.");
        }

        NombreUsuario nuevoUsername = new NombreUsuario(comando.username());
        CorreoInstitucional nuevoCorreo = new CorreoInstitucional(comando.correo());

        DatosInvalidosException errores = new DatosInvalidosException("Revise los datos ingresados.");
        if (usuarios.existeUsername(nuevoUsername.valor(), usuario.getId())) {
            errores.agregar("username", "El nombre de usuario ya esta en uso.");
        }
        if (usuarios.existeCorreo(nuevoCorreo.valor(), usuario.getId())) {
            errores.agregar("correo", "El correo institucional ya esta registrado.");
        }
        if (errores.tieneErrores()) {
            throw errores;
        }

        usuario.actualizarCredencialesPropias(nuevoUsername, nuevoCorreo);
        usuarios.guardar(usuario);

        return construirSesion(usuario);
    }

    private SesionDto construirSesion(Usuario usuario) {
        GeneradorTokens.TokenAcceso token = generadorTokens.emitir(usuario);
        return new SesionDto(
                token.token(),
                "Bearer",
                token.expiraEn(),
                token.expiracionMinutos(),
                usuario.isDebeCambiarPassword(),
                componer(usuario));
    }

    /**
     * Adjunta el nombre de su coordinacion y el de la direccion a la que
     * pertenece: el encabezado los muestra para que el usuario nunca dude de
     * donde esta parado, y el saludo de bienvenida se los dice con todas las
     * letras al entrar (RF-01b).
     */
    private UsuarioDto componer(Usuario usuario) {
        List<UsuarioDto.CoordinacionAsignada> asignadas = usuario.getCoordinaciones().stream()
                .map(id -> estructura.ubicacionDeCoordinacion(id)
                        .map(ubicacion -> new UsuarioDto.CoordinacionAsignada(
                                id, ubicacion.coordinacion(), ubicacion.direccion()))
                        .orElseGet(() -> new UsuarioDto.CoordinacionAsignada(id, null, null)))
                .toList();
        return UsuarioDto.de(usuario, asignadas);
    }

    private Usuario exigirUsuario(Long id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado inexistente."));
    }
}
