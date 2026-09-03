package inictel.edu.pe.iam.application.service;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.iam.application.comando.ActualizarUsuarioComando;
import inictel.edu.pe.iam.application.comando.AsignarPuestoComando;
import inictel.edu.pe.iam.application.comando.RegistrarUsuarioComando;
import inictel.edu.pe.iam.application.dto.AsignacionRealizadaDto;
import inictel.edu.pe.iam.application.dto.PasswordTemporalDto;
import inictel.edu.pe.iam.application.dto.UsuarioDto;
import inictel.edu.pe.iam.domain.model.CorreoInstitucional;
import inictel.edu.pe.iam.domain.model.Dni;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import inictel.edu.pe.iam.domain.model.NombrePersona;
import inictel.edu.pe.iam.domain.model.NombreUsuario;
import inictel.edu.pe.iam.domain.model.Usuario;
import inictel.edu.pe.iam.domain.repository.FiltroUsuarios;
import inictel.edu.pe.iam.domain.repository.UsuarioRepositorio;
import inictel.edu.pe.iam.domain.service.BienesACargo;
import inictel.edu.pe.iam.domain.service.CifradorPassword;
import inictel.edu.pe.iam.domain.service.EstructuraOrganizacional;
import inictel.edu.pe.iam.domain.service.GeneradorPasswordTemporal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Casos de uso de administracion de personas (RF-16 .. RF-30).
 *
 * <p>Concentra las reglas que no caben en el agregado porque necesitan mirar al
 * resto del sistema: unicidad, responsable unico por Coordinacion y la garantia
 * de que siempre quede un Administrador activo.</p>
 *
 * <p><b>Dos actos, no uno.</b> Desde la v3.3 el alta registra a la persona
 * (RF-16b) y la asignacion le da su puesto (RF-28d).</p>
 *
 * <p><b>El puesto ocupado no se sustituye solo (v3.4).</b> Asignar el puesto
 * de Responsable de una Coordinacion que ya lo tiene se rechaza, nombrando al
 * vigente: primero se le da de baja del puesto y despues se nombra a su
 * sucesor (RF-26, RN-08). Un relevo automatico dejaba fuera del cargo a
 * alguien sin que nadie hubiera decidido darlo de baja.</p>
 *
 * <p><b>Y la persona ocupada tampoco (RN-33).</b> El puesto se da a quien esta
 * registrado sin ninguno: quien ya es administrador, responsable u operador no
 * cambia de puesto por el hecho de asignarle otro, y nadie se asigna un puesto
 * a si mismo. Las dos comprobaciones son simetricas —una mira la Coordinacion,
 * la otra a la persona— y por el mismo motivo: cambiar de sitio a alguien tiene
 * consecuencias que deben decidirse, no ocurrir de paso.</p>
 */
@Service
public class GestionUsuariosServicio {

    private final UsuarioRepositorio usuarios;
    private final CifradorPassword cifrador;
    private final EstructuraOrganizacional estructura;
    /** RN-38: los bienes que una persona tiene a su cargo la atan a su puesto. */
    private final BienesACargo bienes;
    private final ContextoUsuario contexto;

    public GestionUsuariosServicio(UsuarioRepositorio usuarios,
                                   CifradorPassword cifrador,
                                   EstructuraOrganizacional estructura,
                                   BienesACargo bienes,
                                   ContextoUsuario contexto) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.estructura = estructura;
        this.bienes = bienes;
        this.contexto = contexto;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /**
     * RF-28, RF-29: el Administrador consulta a quien quiera; el Responsable
     * queda acotado a su propia Coordinacion, se lo pida o no.
     */
    @Transactional(readOnly = true)
    public Pagina<UsuarioDto> buscar(FiltroUsuarios filtro, CriterioPagina criterio) {
        return usuarios.buscar(acotar(filtro), criterio).mapear(this::componer);
    }

    /** RF-29: todos los integrantes de una Coordinacion en una sola seccion. */
    @Transactional(readOnly = true)
    public List<UsuarioDto> listarIntegrantes(Long coordinacionId, boolean soloActivos) {
        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.puedeLeerCoordinacion(coordinacionId)) {
            throw new AccesoDenegadoException("No tiene acceso al personal de otra coordinacion.");
        }
        return usuarios.listarPorCoordinacion(coordinacionId, soloActivos).stream()
                .map(this::componer)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtener(Long id) {
        Usuario usuario = exigirUsuario(id);
        exigirVisibilidad(usuario);
        return componer(usuario);
    }

    // ------------------------------------------------------------------
    // RF-16b: registro previo
    // ------------------------------------------------------------------

    /**
     * Registra a una persona sin puesto.
     *
     * <p>Queda en el sistema con sus datos y sin poder entrar: no tiene rol ni
     * contrasena utilizable. Es deliberado —el alta responde a "quien es" y la
     * asignacion, despues, a "que hace y donde"—, y evita el formulario que
     * obligaba a decidirlo todo el primer dia, cuando muchas veces aun no se
     * sabe donde va a trabajar.</p>
     */
    @Transactional
    public UsuarioDto crear(RegistrarUsuarioComando comando) {
        exigirPermisoDeAlta();
        return componer(registrarPersona(comando));
    }

    /**
     * RF-16e: registra a la persona y le da su puesto en un solo acto y una
     * sola transaccion (v3.9).
     *
     * <p>Es lo que hace el Responsable al sumar un Operador a su equipo: alli
     * el puesto no esta en duda —solo puede crear operadores, y solo en la
     * Coordinacion que administra—, de modo que preguntarlo seria preguntar lo
     * que ya se sabe. Hasta la v3.8 el cliente encadenaba dos peticiones, el
     * alta y la asignacion, y si la segunda fallaba la primera quedaba hecha:
     * la persona se quedaba registrada <b>sin puesto y sin coordinacion</b>, y
     * el Responsable no podia ni verla —su lista esta acotada a su
     * Coordinacion y quien no tiene ninguna no aparece en ella (RN-23)— ni
     * volver a registrarla, porque su DNI, su correo y su usuario ya estaban
     * ocupados. El alta se convertia en un callejon sin salida que solo el
     * Administrador podia deshacer.</p>
     *
     * <p>Aqui los dos actos comparten transaccion: o queda registrada con su
     * puesto y su contrasena, o no queda nada (RNF-17b).</p>
     */
    @Transactional
    public AsignacionRealizadaDto registrarConPuesto(RegistrarUsuarioComando alta,
                                                     AsignarPuestoComando puesto) {
        exigirPermisoDeAlta();

        Rol rol = puesto.rol();
        if (rol == null) {
            throw new DatosInvalidosException("rol", "Seleccione el rol que va a desempenar.");
        }
        exigirPermisoDeAsignacion(rol, puesto.coordinacionId());

        // Se comprueba antes de tocar nada: quien registra debe enterarse de
        // que la coordinacion no sirve mientras el formulario sigue delante,
        // no despues de haber dado de alta a la persona.
        if (rol != Rol.ADMIN) {
            validarCoordinacionUtilizable(puesto.coordinacionId());
        }

        return asignarPuesto(registrarPersona(alta), rol, puesto.coordinacionId());
    }

    /** El alta propiamente dicha, sin la comprobacion de permisos. */
    private Usuario registrarPersona(RegistrarUsuarioComando comando) {
        NombreUsuario username = new NombreUsuario(comando.username());
        CorreoInstitucional correo = new CorreoInstitucional(comando.correo());
        Dni dni = new Dni(comando.dni());
        NombrePersona nombre = new NombrePersona(comando.nombres(), comando.primerApellido(),
                comando.segundoApellido());

        validarUnicidad(username, correo, dni, null);

        // Nadie conoce esta contrasena, ni siquiera quien da el alta: la cuenta
        // no puede usarse hasta que la asignacion genere la temporal de verdad.
        String hashInutilizable = cifrador.cifrar(GeneradorPasswordTemporal.generar());
        Usuario usuario = Usuario.registrar(username, nombre, dni, comando.cargo(), correo, hashInutilizable);

        return usuarios.guardar(usuario);
    }

    // ------------------------------------------------------------------
    // RF-21: edicion de los datos personales
    // ------------------------------------------------------------------

    @Transactional
    public UsuarioDto editar(ActualizarUsuarioComando comando) {
        Usuario usuario = exigirUsuario(comando.id());
        exigirPermisoDeGestion(usuario);

        NombreUsuario username = new NombreUsuario(comando.username());
        CorreoInstitucional correo = new CorreoInstitucional(comando.correo());
        Dni dni = new Dni(comando.dni());
        NombrePersona nombre = new NombrePersona(comando.nombres(), comando.primerApellido(),
                comando.segundoApellido());

        validarUnicidad(username, correo, dni, usuario.getId());

        usuario.actualizarDatos(username, nombre, dni, comando.cargo(), correo);

        return componer(usuarios.guardar(usuario));
    }

    // ------------------------------------------------------------------
    // RF-28d: asignacion de puestos
    // ------------------------------------------------------------------

    /**
     * Da a una persona registrada un puesto: un rol y, si es operativo, la
     * Coordinacion donde lo ejerce.
     *
     * <p>Es el momento en que la persona pasa a poder entrar al sistema, asi
     * que aqui nace su contrasena temporal, que se muestra una sola vez
     * (RF-06). Quien ya tenia credenciales —porque ocupo antes otro puesto y
     * se le dio de baja— conserva las suyas: darle otras seria cerrarle la
     * sesion sin avisar.</p>
     *
     * <p>Se rechaza en tres casos, y los tres por lo mismo —nadie pierde su
     * puesto de paso—: si el de Responsable de esa Coordinacion ya esta ocupado
     * (RN-06, RN-08), si la persona ya tiene alguno (RN-33) y si quien asigna
     * es la propia persona. Darle de baja es siempre una decision aparte.</p>
     */
    @Transactional
    public AsignacionRealizadaDto asignar(AsignarPuestoComando comando) {
        Usuario usuario = exigirUsuario(comando.usuarioId());
        Rol rol = comando.rol();
        if (rol == null) {
            throw new DatosInvalidosException("rol", "Seleccione el rol que va a desempenar.");
        }
        exigirPermisoDeAsignacion(rol, comando.coordinacionId());
        exigirQueNoSeaElPropioUsuario(usuario,
                "No puede asignarse un puesto a usted mismo. Pidaselo a otro administrador.");
        exigirPersonaLibre(usuario);

        return asignarPuesto(usuario, rol, comando.coordinacionId());
    }

    /**
     * El puesto propiamente dicho, ya comprobados los permisos y que la
     * persona esta libre.
     *
     * <p>Lo comparten la asignacion sobre alguien ya registrado (RF-28d) y el
     * alta con puesto en un solo acto (RF-16e): la regla de que la contrasena
     * nace con el puesto, y de que solo nace una vez, se enuncia aqui y no en
     * cada camino.</p>
     */
    private AsignacionRealizadaDto asignarPuesto(Usuario usuario, Rol rol, Long coordinacionId) {
        boolean estrena = usuario.estaSinAsignar();

        if (rol == Rol.ADMIN) {
            usuario.asignarComoAdministrador();
            Asignada resultado = guardarConPassword(usuario, estrena);
            return new AsignacionRealizadaDto(
                    componer(resultado.usuario()),
                    resultado.password(),
                    "'" + resultado.usuario().nombreCompleto() + "' es ahora administrador del sistema. "
                            + "Ve el inventario de todas las coordinaciones, en modo consulta.");
        }

        validarCoordinacionUtilizable(coordinacionId);

        if (rol == Rol.RESPONSABLE) {
            exigirPuestoDeResponsableLibre(coordinacionId, usuario);
        }

        usuario.asignarA(coordinacionId, rol);
        Asignada resultado = guardarConPassword(usuario, estrena);
        Usuario guardado = resultado.usuario();

        String mensaje = "'" + guardado.nombreCompleto() + "' queda como " + etiqueta(rol) + " de "
                + nombreCoordinacionODefecto(coordinacionId) + ".";

        return new AsignacionRealizadaDto(componer(guardado), resultado.password(), mensaje);
    }

    /**
     * Retira a una persona de su Coordinacion (RF-28d).
     *
     * <p>Es tambien la <b>baja del Responsable</b> (RF-26): cuando quien
     * renuncia al cargo es el Responsable vigente, esta operacion deja el
     * puesto libre para que otro pueda ocuparlo. La Coordinacion queda
     * marcada como parada —sin responsable no se registran ni se prestan
     * equipos (RN-07)— y esa marca es lo que reclama el nombramiento del
     * sucesor.</p>
     *
     * <p>La persona vuelve a quedar registrada pero sin puesto. No se le
     * desactiva: dejar un puesto no es dejar la institucion, y su historial en
     * los bienes y prestamos sigue nombrandola (RNF-47).</p>
     */
    @Transactional
    public UsuarioDto retirarDe(Long usuarioId, Long coordinacionId) {
        Usuario usuario = exigirUsuario(usuarioId);
        exigirPermisoDeBaja(usuario, coordinacionId);
        exigirQueNoTengaBienesACargo(usuario, "retirarlo de su coordinacion");

        usuario.retirarDe(coordinacionId);

        return componer(usuarios.guardar(usuario));
    }

    // ------------------------------------------------------------------
    // RF-22, RF-22b, RF-23: estado de la cuenta. Nunca eliminacion (RN-09)
    // ------------------------------------------------------------------

    /**
     * Lleva la cuenta al estado indicado (RF-22b).
     *
     * <p>Son tres, y la diferencia entre dos de ellos es la que la v3.7 vino a
     * poner: la <b>suspension</b> es temporal —vacaciones, un permiso— y
     * conserva el puesto, porque la persona vuelve a el; la <b>baja</b> es la
     * salida de la institucion y libera el puesto, porque quien se fue no
     * responde por un inventario (RN-34). Antes las dos cosas compartian un
     * unico interruptor y no habia manera de saber a cual de los dos se
     * esperaba de vuelta.</p>
     *
     * <p>El Administrador lo hace sobre cualquiera —responsables, operadores y
     * otros administradores— con dos limites: no sobre si mismo, y nunca
     * dejando al sistema sin un Administrador activo (RF-25). El Responsable,
     * solo sobre los Operadores de su Coordinacion (RF-29).</p>
     */
    @Transactional
    public UsuarioDto cambiarEstado(Long id, EstadoCuenta destino) {
        if (destino == null) {
            throw new DatosInvalidosException("estado", "Indique el estado de la cuenta.");
        }
        Usuario usuario = exigirUsuario(id);
        exigirPermisoDeGestion(usuario);

        if (destino != EstadoCuenta.ACTIVA) {
            exigirQueNoSeaElPropioUsuario(usuario, destino == EstadoCuenta.BAJA
                    ? "No puede darse de baja a usted mismo. Pidaselo a otro administrador."
                    : "No puede suspender su propia cuenta.");
            // RF-25: el sistema no se queda sin quien lo administre, ni por una
            // suspension de un mes ni por una baja definitiva.
            if (usuario.esAdmin()) {
                exigirQueQuedeUnAdministrador(usuario);
            }
        }

        // RN-38: la baja libera el puesto, y con el la Coordinacion. Quien
        // tiene equipos a su nombre no puede irse dejandolos sin nadie que
        // responda por ellos. La suspension no lo exige: conserva el puesto y
        // la persona vuelve a el, asi que sus equipos siguen siendo suyos.
        if (destino == EstadoCuenta.BAJA) {
            exigirQueNoTengaBienesACargo(usuario, "darle de baja");
        }

        switch (destino) {
            case SUSPENDIDA -> usuario.suspender();
            case BAJA -> usuario.darDeBaja();
            // Reactivar y reincorporar son la misma peticion —"que vuelva a
            // entrar"— y el agregado sabe cual de las dos le toca segun de
            // donde venga: la reincorporacion, ademas, no le devuelve el puesto.
            case ACTIVA -> {
                if (usuario.estaDeBaja()) {
                    usuario.reincorporar();
                } else {
                    usuario.reactivar();
                }
            }
        }

        return componer(usuarios.guardar(usuario));
    }

    // ------------------------------------------------------------------
    // RF-26, RF-26b: cambio de Responsable de una Coordinacion
    // ------------------------------------------------------------------

    /**
     * Pone a otra persona al frente del inventario de una Coordinacion.
     *
     * <p>Es <b>el</b> camino para cambiar de puesto dentro de la institucion, y
     * por eso vive en la Coordinacion y no en la ficha de la persona: lo que se
     * decide aqui no es que hace fulano, sino quien responde por estos equipos.
     * Puede tomarlo quien no tiene puesto, un Operador <b>de esta misma</b>
     * Coordinacion —que asciende sin cambiar de sitio— o un Administrador que
     * baja al terreno. Nunca quien ya es Responsable de otra: nadie responde
     * por dos inventarios, y soltar el suyo de paso dejaria una Coordinacion
     * parada sin que nadie lo hubiera decidido (RN-35).</p>
     *
     * <p>El Responsable saliente <b>queda libre</b> en el mismo acto: sin rol y
     * sin Coordinacion, con su cuenta intacta y su historial en los bienes que
     * pasaron por sus manos (RF-26, RNF-47). Es lo que distingue este relevo de
     * la baja del puesto: alli la Coordinacion se quedaba parada esperando
     * sucesor; aqui el sucesor ya esta, asi que no hay ni un minuto sin
     * responsable (RN-07).</p>
     */
    @Transactional
    public AsignacionRealizadaDto cambiarResponsable(Long coordinacionId, Long nuevoResponsableId) {
        exigirAdministrador("Solo el Administrador cambia el responsable de una coordinacion.");
        validarCoordinacionUtilizable(coordinacionId);

        Usuario entrante = exigirUsuario(nuevoResponsableId);
        exigirCandidaturaValida(entrante, coordinacionId);

        // RF-25: si quien toma la coordinacion es administrador, deja de serlo,
        // y el sistema no puede quedarse sin ninguno.
        if (entrante.esAdmin()) {
            exigirQueQuedeUnAdministrador(entrante);
        }

        Usuario saliente = usuarios.buscarResponsableDe(coordinacionId).orElse(null);
        String mensajeSaliente = "";
        if (saliente != null) {
            if (saliente.getId().equals(entrante.getId())) {
                throw new ReglaNegocioException(
                        "'" + saliente.nombreCompleto() + "' ya es el responsable de esa coordinacion.");
            }
            saliente.liberarPuesto();
            usuarios.guardar(saliente);
            mensajeSaliente = " '" + saliente.nombreCompleto() + "' deja el cargo y queda sin puesto, "
                    + "con su cuenta activa.";
        }

        // RN-37: si el entrante era un Operador de esta misma Coordinacion,
        // los equipos que tenia a su nombre dejan de estar asignados. No
        // cambian de manos —los sigue llevando el— sino de titulo: ahora los
        // lleva como Responsable, que es lo que significa un bien sin
        // asignacion. Ademas la asignacion a la que apuntaban se reescribe al
        // cambiar de rol, y un bien no puede quedar apuntando a una fila que
        // ya no existe (seccion 5).
        if (entrante.esOperador()) {
            bienes.devolverAlResponsableLosDe(entrante.getId());
        }

        boolean estrena = entrante.estaSinAsignar();
        entrante.asumirResponsabilidadDe(coordinacionId);
        Asignada resultado = guardarConPassword(entrante, estrena);

        String mensaje = "'" + resultado.usuario().nombreCompleto() + "' es ahora responsable de "
                + nombreCoordinacionODefecto(coordinacionId) + "." + mensajeSaliente;

        return new AsignacionRealizadaDto(componer(resultado.usuario()), resultado.password(), mensaje);
    }

    /** RF-26b: quienes pueden tomar el puesto, para que se elija entre ellos. */
    @Transactional(readOnly = true)
    public List<UsuarioDto> candidatosAResponsable(Long coordinacionId) {
        exigirAdministrador("Solo el Administrador cambia el responsable de una coordinacion.");
        return usuarios.candidatosAResponsableDe(coordinacionId).stream()
                .map(this::componer)
                .toList();
    }

    /**
     * RN-35: los tres perfiles que pueden tomar el puesto, y ni uno mas.
     *
     * <p>El mensaje nombra el motivo porque cada caso se resuelve de una
     * manera distinta: al Responsable de otra Coordinacion hay que relevarlo
     * alli primero, y al Operador ajeno hay que darle de baja su puesto.</p>
     */
    private void exigirCandidaturaValida(Usuario entrante, Long coordinacionId) {
        if (!entrante.estaActiva()) {
            throw new ReglaNegocioException(
                    "'" + entrante.nombreCompleto() + "' no tiene la cuenta activa, asi que no puede "
                            + "hacerse cargo de una coordinacion.");
        }
        if (entrante.esResponsable()) {
            throw new ReglaNegocioException(
                    "'" + entrante.nombreCompleto() + "' ya es responsable de "
                            + coordinacionesDe(entrante) + ". Nadie responde por dos inventarios: "
                            + "cambie primero el responsable de aquella coordinacion.");
        }
        if (entrante.esOperador() && !entrante.perteneceA(coordinacionId)) {
            throw new ReglaNegocioException(
                    "'" + entrante.nombreCompleto() + "' es operador de " + coordinacionesDe(entrante)
                            + ". Solo puede ascender a responsable de la coordinacion en la que ya "
                            + "trabaja; para moverlo, dele antes de baja ese puesto.");
        }
    }

    // ------------------------------------------------------------------
    // RF-06: restablecimiento de contrasena por el Administrador
    // ------------------------------------------------------------------

    @Transactional
    public PasswordTemporalDto restablecerPassword(Long id) {
        Usuario usuario = exigirUsuario(id);
        exigirPermisoDeGestion(usuario);
        if (usuario.estaSinAsignar()) {
            throw new ReglaNegocioException(
                    "'" + usuario.nombreCompleto() + "' todavia no tiene un puesto asignado, asi que "
                            + "aun no puede entrar al sistema. Su contrasena nacera al asignarlo.");
        }

        String temporal = GeneradorPasswordTemporal.generar();
        usuario.asignarPasswordTemporal(cifrador.cifrar(temporal));
        Usuario guardado = usuarios.guardar(usuario);

        return PasswordTemporalDto.de(guardado.getId(), guardado.getUsername().valor(), temporal);
    }

    /** RF-08: libera el bloqueo temporal por intentos fallidos. */
    @Transactional
    public UsuarioDto desbloquear(Long id) {
        Usuario usuario = exigirUsuario(id);
        exigirPermisoDeGestion(usuario);
        usuario.desbloquear();

        return componer(usuarios.guardar(usuario));
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    /**
     * Persona guardada junto a la contrasena que acaba de nacer, si nacio.
     *
     * <p>La contrasena viaja fuera del agregado a proposito: es un dato
     * efimero que solo existe para mostrarse una vez a quien asigna, y el
     * agregado no debe cargar con estado que no le pertenece. Tampoco puede
     * ser un campo del servicio, que es compartido por todas las peticiones.</p>
     */
    private record Asignada(Usuario usuario, String password) {
    }

    /**
     * RF-06: la contrasena nace con el puesto. Quien ya tenia credenciales
     * conserva las suyas: darle otras seria cerrarle la sesion sin avisar.
     */
    private Asignada guardarConPassword(Usuario usuario, boolean estrena) {
        String password = null;
        if (estrena) {
            password = GeneradorPasswordTemporal.generar();
            usuario.asignarPasswordTemporal(cifrador.cifrar(password));
        }
        return new Asignada(usuarios.guardar(usuario), password);
    }

    /**
     * RN-33: solo se asigna un puesto a quien no tiene ninguno.
     *
     * <p>El agregado sostiene la regla (Usuario.asignarA); aqui se enuncia con
     * los nombres delante —el rol que ya tiene y la Coordinacion donde lo
     * ejerce— y se dice donde se le da de baja, que es lo que hay que hacer
     * antes si de verdad se le quiere mover (RN-08).</p>
     */
    private void exigirPersonaLibre(Usuario usuario) {
        if (usuario.estaSinAsignar() && usuario.getCoordinaciones().isEmpty()) {
            return;
        }
        if (usuario.esAdmin() || usuario.estaSinAsignar()) {
            throw new ReglaNegocioException(
                    "'" + usuario.nombreCompleto() + "' ya tiene puesto en el sistema. Un puesto solo "
                            + "se asigna a quien esta registrado sin ninguno.");
        }
        String donde = usuario.esResponsable()
                ? "Dele de baja del puesto desde la ficha de su coordinacion, en Direcciones, y "
                        + "despues asignele el nuevo."
                : "Dele de baja del puesto en esta misma ventana y despues asignele el nuevo.";
        throw new ReglaNegocioException(
                "'" + usuario.nombreCompleto() + "' ya es " + etiqueta(usuario.getRol()) + " de "
                        + coordinacionesDe(usuario) + ". " + donde);
    }

    /**
     * RN-06, RN-08: el puesto de Responsable debe estar libre para poder darlo.
     *
     * <p>Una Coordinacion tiene como maximo un Responsable, y el que esta no
     * sale por el hecho de que llegue otro: se le da de baja del puesto antes,
     * desde la tarjeta de su Coordinacion, y solo entonces el puesto admite un
     * sucesor. Hasta la v3.3 la asignacion relevaba sola al vigente, de modo
     * que una persona podia perder su cargo como efecto colateral de nombrar a
     * otra.</p>
     */
    private void exigirPuestoDeResponsableLibre(Long coordinacionId, Usuario entrante) {
        Optional<Usuario> vigente = usuarios.buscarResponsableDe(coordinacionId);
        if (vigente.isEmpty()) {
            return;
        }
        Usuario ocupante = vigente.get();
        if (ocupante.getId().equals(entrante.getId())) {
            throw new ReglaNegocioException(
                    "'" + ocupante.nombreCompleto() + "' ya es el responsable de esa coordinacion.");
        }
        throw new ReglaNegocioException(
                nombreCoordinacionODefecto(coordinacionId) + " ya tiene responsable: '"
                        + ocupante.nombreCompleto() + "'. Dele de baja del puesto en la pantalla de "
                        + "Direcciones y despues nombre a quien lo sucede.");
    }

    private UsuarioDto componer(Usuario usuario) {
        List<UsuarioDto.CoordinacionAsignada> asignadas = usuario.getCoordinaciones().stream()
                .map(id -> estructura.ubicacionDeCoordinacion(id)
                        .map(ubicacion -> new UsuarioDto.CoordinacionAsignada(
                                id, ubicacion.coordinacion(), ubicacion.direccion()))
                        .orElseGet(() -> new UsuarioDto.CoordinacionAsignada(id, null, null)))
                .toList();
        return UsuarioDto.de(usuario, asignadas);
    }

    private String nombreCoordinacion(Long coordinacionId) {
        return coordinacionId == null ? null
                : estructura.ubicacionDeCoordinacion(coordinacionId)
                        .map(EstructuraOrganizacional.Ubicacion::coordinacion)
                        .orElse(null);
    }

    private String nombreCoordinacionODefecto(Long coordinacionId) {
        String nombre = nombreCoordinacion(coordinacionId);
        return nombre == null ? "su coordinacion" : "la coordinacion '" + nombre + "'";
    }

    /** Las coordinaciones de una persona, nombradas, para las frases de aviso. */
    private String coordinacionesDe(Usuario usuario) {
        List<String> nombres = usuario.getCoordinaciones().stream()
                .map(this::nombreCoordinacion)
                .filter(nombre -> nombre != null)
                .toList();
        return nombres.isEmpty() ? "su coordinacion" : "'" + String.join("', '", nombres) + "'";
    }

    /** RN-23: el Responsable solo ve a su propia gente. */
    private FiltroUsuarios acotar(FiltroUsuarios filtro) {
        UsuarioAutenticado actual = contexto.requerido();
        return actual.esAdmin() ? filtro : filtro.acotadoA(actual.coordinacionRequerida());
    }

    private void exigirVisibilidad(Usuario usuario) {
        UsuarioAutenticado actual = contexto.requerido();
        if (actual.esAdmin()) {
            return;
        }
        boolean compartenCoordinacion = usuario.getCoordinaciones().stream()
                .anyMatch(actual::puedeLeerCoordinacion);
        if (!compartenCoordinacion) {
            throw new AccesoDenegadoException("No tiene acceso a los datos de otra coordinacion.");
        }
    }

    /**
     * RF-16b: el registro previo lo hacen el Administrador y el Responsable.
     *
     * <p>El Responsable lo necesita para dar de alta a su propia gente; quien
     * queda registrado no tiene ningun permiso hasta que se le asigne un
     * puesto, de modo que el alta por si sola no abre nada.</p>
     */
    private void exigirPermisoDeAlta() {
        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.esAdmin() && !actual.esResponsable()) {
            throw new AccesoDenegadoException("No tiene permisos para registrar personas.");
        }
    }

    /**
     * RF-28d: quien puede repartir puestos.
     *
     * <p>El Administrador reparte todos. El Responsable solo puede sumar
     * Operadores a una Coordinacion suya: nombrar responsables —y por tanto
     * relevar al vigente— es decision del Administrador, y nombrar
     * administradores, mas todavia.</p>
     */
    private void exigirPermisoDeAsignacion(Rol rol, Long coordinacionId) {
        UsuarioAutenticado actual = contexto.requerido();
        if (actual.esAdmin()) {
            return;
        }
        if (!actual.esResponsable()) {
            throw new AccesoDenegadoException("No tiene permisos para asignar puestos.");
        }
        if (rol != Rol.OPERADOR) {
            throw new AccesoDenegadoException(
                    "Solo el administrador asigna responsables y administradores.");
        }
        actual.exigirAccesoA(coordinacionId);
    }

    /**
     * RF-26, RF-28d: quien puede retirar a alguien de una Coordinacion.
     *
     * <p>El Administrador da de baja a cualquiera, y es el unico que puede dar
     * de baja a un Responsable: dejar una Coordinacion sin quien responda por
     * sus bienes es una decision institucional, no del propio interesado ni de
     * su equipo. El Responsable solo retira a los Operadores de la suya.</p>
     */
    private void exigirPermisoDeBaja(Usuario objetivo, Long coordinacionId) {
        UsuarioAutenticado actual = contexto.requerido();
        if (actual.esAdmin()) {
            return;
        }
        if (!actual.esResponsable()) {
            throw new AccesoDenegadoException("No tiene permisos para retirar puestos.");
        }
        if (!objetivo.esOperador()) {
            throw new AccesoDenegadoException(
                    "Solo el administrador da de baja a un responsable de su coordinacion.");
        }
        actual.exigirAccesoA(coordinacionId);
    }

    /** El Responsable solo gestiona a los Operadores de su Coordinacion. */
    private void exigirPermisoDeGestion(Usuario objetivo) {
        UsuarioAutenticado actual = contexto.requerido();
        if (actual.esAdmin()) {
            return;
        }
        if (!actual.esResponsable()) {
            throw new AccesoDenegadoException("No tiene permisos para gestionar usuarios.");
        }
        boolean suyo = objetivo.getCoordinaciones().stream().anyMatch(actual::puedeLeerCoordinacion);
        if (!objetivo.esOperador() || !suyo) {
            throw new AccesoDenegadoException(
                    "Solo puede gestionar a los operadores de su coordinacion.");
        }
    }

    private Usuario exigirUsuario(Long id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el usuario", id));
    }

    /** RF-24: unicidad de nombre de usuario, correo y DNI. */
    private void validarUnicidad(NombreUsuario username, CorreoInstitucional correo, Dni dni, Long idActual) {
        DatosInvalidosException errores = new DatosInvalidosException("Revise los datos ingresados.");

        if (usuarios.existeUsername(username.valor(), idActual)) {
            errores.agregar("username", "El nombre de usuario ya esta en uso.");
        }
        if (usuarios.existeCorreo(correo.valor(), idActual)) {
            errores.agregar("correo", "El correo institucional ya esta registrado.");
        }
        if (usuarios.existeDni(dni.valor(), idActual)) {
            errores.agregar("dni", "El DNI ya esta registrado en el sistema.");
        }
        if (errores.tieneErrores()) {
            throw errores;
        }
    }

    private void validarCoordinacionUtilizable(Long coordinacionId) {
        if (coordinacionId == null) {
            throw new DatosInvalidosException("coordinacionId",
                    "Seleccione la coordinacion del puesto.");
        }
        if (!estructura.existeCoordinacionActiva(coordinacionId)) {
            throw new DatosInvalidosException("coordinacionId",
                    "La coordinacion seleccionada no existe o esta desactivada.");
        }
    }

    /**
     * RF-25: siempre queda al menos un Administrador activo.
     *
     * <p>Se comprueba ante todo lo que puede quitar uno de en medio: la
     * suspension, la baja y el nombramiento como Responsable de una
     * Coordinacion, que tambien le quita el cargo. Sin esta regla, el sistema
     * admite quedarse sin nadie que pueda repartir puestos, y la unica salida
     * seria entrar en la base de datos a mano.</p>
     */
    private void exigirQueQuedeUnAdministrador(Usuario usuario) {
        if (!usuario.estaActiva()) {
            return;
        }
        if (usuarios.contarAdministradoresActivos() <= 1) {
            throw new ReglaNegocioException(
                    "'" + usuario.nombreCompleto() + "' es el unico Administrador activo del sistema. "
                            + "Nombre a otro antes, porque sin ninguno no habria quien reparta los "
                            + "puestos ni quien organice la institucion.");
        }
    }

    /**
     * RN-38: quien tiene bienes a su cargo no deja su puesto.
     *
     * <p>La regla no es un capricho administrativo: un equipo a nombre de
     * alguien que ya no trabaja en la Coordinacion es un equipo del que nadie
     * responde, y el inventario diria lo contrario. El mensaje dice cuantos
     * son y cual es la salida, que es siempre la misma y siempre del
     * Responsable: reasignarlos a otro Operador o quedarselos el (RF-83).</p>
     *
     * <p>La base sostiene la regla por su cuenta: la fila de
     * {@code usuario_coordinacion} de esta persona esta referenciada por sus
     * bienes, de modo que ni un error de programacion futuro podria borrarla
     * (seccion 5). Aqui se comprueba antes para poder explicarlo.</p>
     */
    private void exigirQueNoTengaBienesACargo(Usuario usuario, String queSeIbaAHacer) {
        long cuantos = bienes.contarDe(usuario.getId());
        if (cuantos == 0) {
            return;
        }
        throw new ReglaNegocioException(
                "'" + usuario.nombreCompleto() + "' tiene " + cuantos
                        + (cuantos == 1 ? " equipo a su cargo" : " equipos a su cargo")
                        + ", asi que no se puede " + queSeIbaAHacer + " todavia. "
                        + "Desde la ficha de cada equipo, el responsable de la coordinacion debe "
                        + "entregarselos a otro operador o quedarselos el.");
    }

    /** RF-26b: hay operaciones que solo toma el Administrador. */
    private void exigirAdministrador(String mensaje) {
        if (!contexto.requerido().esAdmin()) {
            throw new AccesoDenegadoException(mensaje);
        }
    }

    /**
     * RF-25, RN-33: nadie decide sobre su propia cuenta.
     *
     * <p>Ni desactivarse —el sistema se quedaria sin quien lo administre— ni
     * darse a si mismo un puesto: quien reparte los puestos no es quien los
     * recibe.</p>
     */
    private void exigirQueNoSeaElPropioUsuario(Usuario usuario, String mensaje) {
        contexto.id()
                .filter(idActual -> idActual.equals(usuario.getId()))
                .ifPresent(idActual -> {
                    throw new ReglaNegocioException(mensaje);
                });
    }

    /** Nombre del rol en las frases dirigidas a las personas (RNF-29). */
    private static String etiqueta(Rol rol) {
        return switch (rol) {
            case ADMIN -> "administrador";
            case RESPONSABLE -> "responsable";
            case OPERADOR -> "operador";
        };
    }
}
