package inictel.edu.pe.iam.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import inictel.edu.pe.compartido.domain.seguridad.Rol;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Raiz del agregado Usuario (RF-16 .. RF-30).
 *
 * <p>Modelo de dominio puro: no conoce JPA, Spring ni la capa web. Concentra
 * las reglas que dependen unicamente del propio usuario —coherencia entre rol
 * y asignaciones, bloqueo por intentos fallidos, activacion—; las que
 * necesitan consultar a otros usuarios (unicidad, responsable unico por
 * Coordinacion, "siempre un administrador activo") viven en la capa de
 * aplicacion, que si tiene acceso al repositorio.</p>
 *
 * <p><b>Registro previo (RF-16b).</b> Una persona puede existir sin rol y sin
 * ninguna coordinacion: es alguien a quien se dio de alta antes de saber donde
 * va a trabajar. Mientras esta asi no puede entrar al sistema, y su contrasena
 * ni siquiera existe todavia. La asignacion (RF-28d) es la que le da rol,
 * coordinacion y credenciales, en un solo acto.</p>
 *
 * <p><b>Una sola coordinacion (RN-05).</b> Toda persona con puesto operativo
 * —RESPONSABLE u OPERADOR— pertenece a exactamente una Coordinacion. El ADMIN
 * no pertenece a ninguna: su ambito es la institucion entera, en consulta.
 * Mover a alguien de coordinacion es retirarlo de la que tiene y asignarlo a
 * la nueva, en ese orden, para que nadie figure en dos inventarios a la
 * vez.</p>
 */
public class Usuario {

    private static final int LONGITUD_MAXIMA_CARGO = 150;

    /** RF-06b: cargo con el que queda la primera persona de la plataforma. */
    public static final String CARGO_ADMINISTRADOR_SISTEMA = "Administrador del sistema";

    private Long id;
    private NombreUsuario username;
    private NombrePersona nombre;
    private Dni dni;
    private String cargo;
    private CorreoInstitucional correo;
    private String passwordHash;
    /** RF-16b: null mientras la persona esta solo registrada, sin asignar. */
    private Rol rol;
    private final Set<Long> coordinaciones = new LinkedHashSet<>();
    private EstadoCuenta estado;
    private boolean debeCambiarPassword;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Usuario() {
    }

    /**
     * RF-16b: registro previo de una persona.
     *
     * <p>Queda registrada con sus datos personales y sus credenciales de
     * identificacion —usuario y correo—, pero sin rol, sin coordinacion y sin
     * una contrasena que sirva para entrar. Es deliberado: el alta responde a
     * "quien es esta persona" y la asignacion, despues, a "que hace y donde".
     * Separarlas evita el formulario que obligaba a decidirlo todo de golpe,
     * el primer dia, cuando muchas veces aun no se sabe.</p>
     *
     * @param hashInutilizable hash de una contrasena aleatoria que nadie
     *                         conoce: la cuenta no puede usarse hasta que la
     *                         asignacion genere su contrasena temporal
     */
    public static Usuario registrar(NombreUsuario username,
                                    NombrePersona nombre,
                                    Dni dni,
                                    String cargo,
                                    CorreoInstitucional correo,
                                    String hashInutilizable) {
        Usuario usuario = new Usuario();
        usuario.username = exigirUsername(username);
        usuario.nombre = exigirNombre(nombre);
        usuario.dni = exigirDni(dni);
        usuario.cargo = normalizarCargo(cargo);
        usuario.correo = exigirCorreo(correo);
        usuario.rol = null;
        usuario.passwordHash = exigirHash(hashInutilizable);
        usuario.estado = EstadoCuenta.ACTIVA;
        usuario.debeCambiarPassword = true;
        usuario.intentosFallidos = 0;
        usuario.fechaCreacion = LocalDateTime.now();
        return usuario;
    }

    /**
     * Alta de la cuenta administradora inicial (CuentaInicialInitializer).
     *
     * <p>Es el unico usuario que nace con rol y con contrasena, porque no hay
     * nadie que pueda asignarselo: es el primero.</p>
     */
    public static Usuario registrarAdministradorInicial(NombreUsuario username,
                                                        NombrePersona nombre,
                                                        Dni dni,
                                                        CorreoInstitucional correo,
                                                        String passwordHash) {
        Usuario usuario = new Usuario();
        usuario.username = exigirUsername(username);
        usuario.nombre = exigirNombre(nombre);
        usuario.dni = exigirDni(dni);
        usuario.cargo = CARGO_ADMINISTRADOR_SISTEMA;
        usuario.correo = exigirCorreo(correo);
        usuario.rol = Rol.ADMIN;
        usuario.passwordHash = exigirHash(passwordHash);
        usuario.estado = EstadoCuenta.ACTIVA;
        usuario.debeCambiarPassword = true;
        usuario.intentosFallidos = 0;
        usuario.fechaCreacion = LocalDateTime.now();
        return usuario;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Usuario reconstituir(Long id,
                                       NombreUsuario username,
                                       NombrePersona nombre,
                                       Dni dni,
                                       String cargo,
                                       CorreoInstitucional correo,
                                       String passwordHash,
                                       Rol rol,
                                       Collection<Long> coordinaciones,
                                       EstadoCuenta estado,
                                       boolean debeCambiarPassword,
                                       int intentosFallidos,
                                       LocalDateTime bloqueadoHasta,
                                       LocalDateTime ultimoAcceso,
                                       LocalDateTime fechaCreacion,
                                       LocalDateTime fechaActualizacion) {
        Usuario usuario = new Usuario();
        usuario.id = id;
        usuario.username = username;
        usuario.nombre = nombre;
        usuario.dni = dni;
        usuario.cargo = cargo;
        usuario.correo = correo;
        usuario.passwordHash = passwordHash;
        usuario.rol = rol;
        if (coordinaciones != null) {
            usuario.coordinaciones.addAll(coordinaciones);
        }
        usuario.estado = estado == null ? EstadoCuenta.ACTIVA : estado;
        usuario.debeCambiarPassword = debeCambiarPassword;
        usuario.intentosFallidos = intentosFallidos;
        usuario.bloqueadoHasta = bloqueadoHasta;
        usuario.ultimoAcceso = ultimoAcceso;
        usuario.fechaCreacion = fechaCreacion;
        usuario.fechaActualizacion = fechaActualizacion;
        return usuario;
    }

    // ------------------------------------------------------------------
    // Comportamiento
    // ------------------------------------------------------------------

    /**
     * RF-21: actualiza los datos personales que administra el Administrador.
     *
     * <p>El rol y las coordinaciones no se tocan aqui: se cambian asignando y
     * retirando asignaciones, que es donde viven sus reglas.</p>
     */
    public void actualizarDatos(NombreUsuario username,
                                NombrePersona nombre,
                                Dni dni,
                                String cargo,
                                CorreoInstitucional correo) {
        this.username = exigirUsername(username);
        this.nombre = exigirNombre(nombre);
        this.dni = exigirDni(dni);
        this.cargo = normalizarCargo(cargo);
        this.correo = exigirCorreo(correo);
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-06b: primer ingreso. El usuario confirma quien es y define su
     * contrasena definitiva en un solo acto.
     *
     * <p>La cuenta administradora inicial nace con datos de relleno; mientras
     * no se corrijan, el sistema estaria identificando a una persona real con
     * el nombre y el DNI de un marcador de posicion.</p>
     *
     * <p>El nombre se exige entero, segundo apellido incluido: es la identidad
     * con la que queda registrada la primera persona del sistema. El cargo no
     * se pregunta porque no hay nada que elegir: quien estrena la cuenta
     * inicial es el Administrador del sistema.</p>
     */
    public void completarPrimerIngreso(NombrePersona nombre, Dni dni, String nuevoHash) {
        this.nombre = exigirNombre(nombre);
        this.dni = exigirDni(dni);
        this.cargo = CARGO_ADMINISTRADOR_SISTEMA;
        cambiarPassword(nuevoHash);
    }

    /** Cambio de las credenciales propias: nombre de usuario y correo. */
    public void actualizarCredencialesPropias(NombreUsuario username, CorreoInstitucional correo) {
        this.username = exigirUsername(username);
        this.correo = exigirCorreo(correo);
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ------------------------------------------------------------------
    // RF-28d: asignaciones
    // ------------------------------------------------------------------

    /**
     * Asigna a la persona un rol operativo en una Coordinacion.
     *
     * <p>Solo recibe puesto quien esta libre, es decir, quien no tiene ninguno
     * (RN-33): un puesto no se sustituye por otro en el mismo acto. La
     * unicidad del Responsable por Coordinacion (RN-06) la comprueba la capa
     * de aplicacion, que puede consultar a los demas usuarios, y la garantiza
     * ademas un indice unico parcial de la base.</p>
     */
    public void asignarA(Long coordinacion, Rol rolAsignado) {
        exigirCuentaUtilizable();
        exigirPuestoLibre();
        if (coordinacion == null) {
            throw new DatosInvalidosException("coordinacionId", "Seleccione la coordinacion.");
        }
        if (rolAsignado != Rol.RESPONSABLE && rolAsignado != Rol.OPERADOR) {
            throw new DatosInvalidosException("rol",
                    "En una coordinacion solo se asigna el rol de Responsable o el de Operador.");
        }
        this.rol = rolAsignado;
        this.coordinaciones.add(coordinacion);
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-16: designa a la persona Administrador de la institucion.
     *
     * <p>Tambien el puesto de Administrador se da solo a quien esta libre
     * (RN-33): nombrar administrador a un responsable en ejercicio dejaria su
     * Coordinacion parada como efecto colateral de un nombramiento, sin que
     * nadie hubiera decidido darlo de baja. Y el Administrador no pertenece a
     * ninguna Coordinacion: su ambito es toda la institucion, en modo consulta
     * (RN-05, RN-22).</p>
     */
    public void asignarComoAdministrador() {
        exigirCuentaUtilizable();
        exigirPuestoLibre();
        this.rol = Rol.ADMIN;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Retira a la persona de una Coordinacion.
     *
     * <p>Como solo puede tener una (RN-05), retirarla la devuelve al estado en
     * que quedo tras el alta: registrada, pero sin puesto. No se le desactiva
     * —dejar un puesto no es dejar la institucion— y conserva su historial en
     * los bienes y prestamos que llevan su nombre (RNF-47).</p>
     */
    public void retirarDe(Long coordinacion) {
        if (!coordinaciones.remove(coordinacion)) {
            throw new ReglaNegocioException(
                    "'" + nombreCompleto() + "' no tiene ninguna asignacion en esa coordinacion.");
        }
        if (coordinaciones.isEmpty()) {
            this.rol = null;
        }
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** true si la persona esta registrada pero todavia sin rol (RF-16b). */
    public boolean estaSinAsignar() {
        return rol == null;
    }

    /**
     * RN-33: el puesto se da a quien no tiene ninguno.
     *
     * <p>Una persona con rol ya esta trabajando en algo, y cambiarselo en el
     * acto de asignar tiene consecuencias que nadie ha decidido: el
     * responsable dejaria su Coordinacion parada (RN-07) y el operador
     * desapareceria de su inventario. Moverla son dos actos, y en este orden:
     * la baja del puesto que tiene y la asignacion del nuevo (RN-08).</p>
     */
    private void exigirPuestoLibre() {
        if (rol == null && coordinaciones.isEmpty()) {
            return;
        }
        String puesto = rol == null ? "una coordinacion asignada" : "el puesto de " + etiqueta(rol);
        throw new ReglaNegocioException(
                "'" + nombreCompleto() + "' ya tiene " + puesto + ". Solo se asigna un puesto a quien "
                        + "esta registrado sin ninguno: dele de baja el que tiene y despues asignele "
                        + "el nuevo.");
    }

    /**
     * Cuenta lista para trabajar: la asignacion es lo unico que le falta.
     *
     * <p>Dar un puesto a quien no puede entrar deja una Coordinacion a cargo de
     * alguien que no puede atenderla: la suspendida esta fuera por ahora y la
     * de baja ya no pertenece a la institucion (RF-22b).</p>
     */
    private void exigirCuentaUtilizable() {
        if (estado.permiteEntrar()) {
            return;
        }
        String motivo = estado == EstadoCuenta.BAJA
                ? "esta dada de baja de la institucion. Reincorporela antes de asignarle un puesto."
                : "tiene la cuenta suspendida. Reactivela antes de asignarle un puesto.";
        throw new ReglaNegocioException("'" + nombreCompleto() + "' " + motivo);
    }

    /**
     * Cambio de contrasena hecho por el propio usuario: cancela la exigencia de
     * cambio y libera cualquier bloqueo vigente.
     */
    public void cambiarPassword(String nuevoHash) {
        this.passwordHash = exigirHash(nuevoHash);
        this.debeCambiarPassword = false;
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-06: el Administrador asigna una contrasena temporal y fuerza el cambio. */
    public void asignarPasswordTemporal(String nuevoHash) {
        this.passwordHash = exigirHash(nuevoHash);
        this.debeCambiarPassword = true;
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** La contrasena nueva debe ser distinta de la vigente. */
    public boolean tieneMismoHash(String hash) {
        return passwordHash.equals(hash);
    }

    // ------------------------------------------------------------------
    // RF-22, RF-22b: estado de la cuenta. El usuario nunca se elimina (RN-09)
    // ------------------------------------------------------------------

    /**
     * RF-22b: la persona no entra por ahora —vacaciones, permiso, licencia—.
     *
     * <p><b>Conserva su puesto</b>, y es lo que distingue esta operacion de la
     * baja: la suspension es temporal y quien vuelve, vuelve a lo suyo. Una
     * Coordinacion cuyo Responsable esta de vacaciones sigue teniendo
     * Responsable.</p>
     */
    public void suspender() {
        if (estado == EstadoCuenta.BAJA) {
            throw new ReglaNegocioException(
                    "'" + nombreCompleto() + "' ya esta dado de baja de la institucion: no hay nada "
                            + "que suspender. Reincorporelo si vuelve a trabajar aqui.");
        }
        this.estado = EstadoCuenta.SUSPENDIDA;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-22b: termina la suspension. La persona vuelve a su puesto y entra. */
    public void reactivar() {
        if (estado == EstadoCuenta.BAJA) {
            throw new ReglaNegocioException(
                    "'" + nombreCompleto() + "' esta dado de baja de la institucion. Use la "
                            + "reincorporacion, que le devuelve la cuenta pero no el puesto.");
        }
        this.estado = EstadoCuenta.ACTIVA;
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-22b, RN-34: la persona deja la institucion.
     *
     * <p>Y al dejarla <b>deja tambien su puesto</b>: quien ya no trabaja aqui
     * no responde por un inventario ni figura entre los operadores de una
     * Coordinacion. Si era Responsable, la suya queda parada hasta que se
     * nombre a otro, igual que ante cualquier otra salida del cargo
     * (RN-07).</p>
     *
     * <p>No la borra: su nombre sigue en el historial de los bienes y de los
     * prestamos que paso por sus manos (RN-09, RNF-47).</p>
     */
    public void darDeBaja() {
        if (estado == EstadoCuenta.BAJA) {
            throw new ReglaNegocioException(
                    "'" + nombreCompleto() + "' ya esta dado de baja de la institucion.");
        }
        this.estado = EstadoCuenta.BAJA;
        liberarPuesto();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-22b: la persona vuelve a la institucion, sin puesto.
     *
     * <p>Vuelve como quien acaba de ser registrada: con su cuenta y sin cargo,
     * porque el que tenia se cubrio cuando se fue. Asignarselo otra vez es una
     * decision aparte, y se toma como cualquier otra asignacion (RF-28d).</p>
     */
    public void reincorporar() {
        if (estado != EstadoCuenta.BAJA) {
            throw new ReglaNegocioException(
                    "'" + nombreCompleto() + "' no esta dado de baja: no hay nada que reincorporar.");
        }
        this.estado = EstadoCuenta.ACTIVA;
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Deja a la persona <b>libre</b>: sin rol y sin Coordinacion.
     *
     * <p>Es el estado en que queda quien deja un cargo sin tomar otro: el
     * Responsable al que releva un sucesor (RF-26) y quien se va de la
     * institucion (RN-34). La cuenta no se toca: dejar un puesto no es dejar
     * de existir.</p>
     */
    public void liberarPuesto() {
        this.coordinaciones.clear();
        this.rol = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-26: la persona toma el puesto de Responsable de una Coordinacion.
     *
     * <p>Es el unico camino por el que alguien cambia de puesto sin pasar por
     * "libre": lo que traiga —nada, una plaza de operador en esa misma
     * Coordinacion, o el cargo de administrador— lo deja aqui. Lo que no puede
     * traer es <b>otra responsabilidad</b>: quien ya responde por un inventario
     * no puede responder por dos, y soltar el suyo al vuelo dejaria una
     * Coordinacion parada sin que nadie lo hubiera decidido (RN-06, RN-35).</p>
     */
    public void asumirResponsabilidadDe(Long coordinacion) {
        exigirCuentaUtilizable();
        if (coordinacion == null) {
            throw new DatosInvalidosException("coordinacionId", "Seleccione la coordinacion.");
        }
        if (esResponsable()) {
            throw new ReglaNegocioException(
                    "'" + nombreCompleto() + "' ya es responsable de una coordinacion. Nadie responde "
                            + "por dos inventarios a la vez.");
        }
        this.coordinaciones.clear();
        this.coordinaciones.add(coordinacion);
        this.rol = Rol.RESPONSABLE;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RNF-05: contabiliza un intento fallido y bloquea la cuenta al alcanzar el
     * maximo permitido.
     *
     * @return true si el intento provoco el bloqueo de la cuenta
     */
    public boolean registrarIntentoFallido(int maximoIntentos, int minutosBloqueo) {
        this.intentosFallidos++;
        if (this.intentosFallidos >= maximoIntentos) {
            this.bloqueadoHasta = LocalDateTime.now().plusMinutes(minutosBloqueo);
            this.intentosFallidos = 0;
            return true;
        }
        return false;
    }

    public void registrarAccesoExitoso() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
        this.ultimoAcceso = LocalDateTime.now();
    }

    /** Libera el bloqueo temporal por decision del Administrador. */
    public void desbloquear() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public boolean estaBloqueado() {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(LocalDateTime.now());
    }

    /** Minutos que faltan para que expire el bloqueo, para informarlo al usuario. */
    public long minutosDeBloqueoRestantes() {
        if (!estaBloqueado()) {
            return 0;
        }
        return Math.max(1, ChronoUnit.MINUTES.between(LocalDateTime.now(), bloqueadoHasta) + 1);
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean esResponsable() {
        return rol == Rol.RESPONSABLE;
    }

    public boolean esOperador() {
        return rol == Rol.OPERADOR;
    }

    public boolean perteneceA(Long coordinacion) {
        return coordinacion != null && coordinaciones.contains(coordinacion);
    }

    public String nombreCompleto() {
        return nombre.completo();
    }

    // ------------------------------------------------------------------
    // Validaciones internas
    // ------------------------------------------------------------------

    private static NombreUsuario exigirUsername(NombreUsuario valor) {
        if (valor == null) {
            throw new DatosInvalidosException("username", "Ingrese el nombre de usuario.");
        }
        return valor;
    }

    private static NombrePersona exigirNombre(NombrePersona valor) {
        if (valor == null) {
            throw new DatosInvalidosException("nombres", "Ingrese los nombres del usuario.");
        }
        return valor;
    }


    private static Dni exigirDni(Dni valor) {
        if (valor == null) {
            throw new DatosInvalidosException("dni", "Ingrese el DNI.");
        }
        return valor;
    }

    private static CorreoInstitucional exigirCorreo(CorreoInstitucional valor) {
        if (valor == null) {
            throw new DatosInvalidosException("correo", "Ingrese el correo institucional.");
        }
        return valor;
    }

    private static String exigirHash(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El hash de la contrasena no puede estar vacio.");
        }
        return valor;
    }

    private static String normalizarCargo(String cargo) {
        if (cargo == null) {
            return null;
        }
        String limpio = cargo.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > LONGITUD_MAXIMA_CARGO) {
            throw new DatosInvalidosException("cargo", "El cargo no puede superar los 150 caracteres.");
        }
        return limpio;
    }

    /** Nombre del rol en las frases dirigidas a las personas (RNF-29). */
    private static String etiqueta(Rol valor) {
        return switch (valor) {
            case ADMIN -> "administrador";
            case RESPONSABLE -> "responsable";
            case OPERADOR -> "operador";
        };
    }

    // ------------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public NombreUsuario getUsername() {
        return username;
    }

    public NombrePersona getNombre() {
        return nombre;
    }

    public Dni getDni() {
        return dni;
    }

    public String getCargo() {
        return cargo;
    }

    public CorreoInstitucional getCorreo() {
        return correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    /**
     * Coordinaciones en las que la persona tiene asignacion (RF-28d).
     *
     * <p>Es un conjunto porque la asignacion es una relacion en la base, pero
     * RN-05 lo limita a una como maximo: {@link #getCoordinacion()} es la
     * forma habitual de leerlo.</p>
     */
    public Set<Long> getCoordinaciones() {
        return Set.copyOf(coordinaciones);
    }

    /**
     * Coordinacion en la que trabaja la persona, o {@code null} si no tiene
     * puesto (RN-05). El ADMIN nunca tiene ninguna.
     */
    public Long getCoordinacion() {
        return coordinaciones.stream().findFirst().orElse(null);
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    /** RF-07, RN-10: la cuenta activa es la unica que autentica. */
    public boolean estaActiva() {
        return estado.permiteEntrar();
    }

    /** RN-34: la persona dejo la institucion. */
    public boolean estaDeBaja() {
        return estado == EstadoCuenta.BAJA;
    }

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
