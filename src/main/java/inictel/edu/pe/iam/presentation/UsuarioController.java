package inictel.edu.pe.iam.presentation;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.compartido.presentation.PaginaResponse;
import inictel.edu.pe.iam.application.dto.AsignacionRealizadaDto;
import inictel.edu.pe.iam.application.dto.PasswordTemporalDto;
import inictel.edu.pe.iam.application.dto.UsuarioDto;
import inictel.edu.pe.iam.application.service.GestionUsuariosServicio;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import inictel.edu.pe.iam.domain.repository.FiltroUsuarios;
import inictel.edu.pe.iam.presentation.dto.AltaConPuestoRequest;
import inictel.edu.pe.iam.presentation.dto.AsignacionRequest;
import inictel.edu.pe.iam.presentation.dto.CambioEstadoCuentaRequest;
import inictel.edu.pe.iam.presentation.dto.CambioResponsableRequest;
import inictel.edu.pe.iam.presentation.dto.UsuarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestion de personas (RF-16 .. RF-30).
 *
 * <p>Casi todo es exclusivo del Administrador. Las excepciones son las
 * operaciones que el Responsable ejerce sobre los Operadores de su propia
 * Coordinacion (RF-29); en ellas el servicio vuelve a comprobar el ambito, de
 * modo que la anotacion nunca es la unica defensa.</p>
 *
 * <p><b>Dos actos, no uno (v3.3).</b> El alta registra a la persona -quien
 * es- y la asignacion le da su puesto -que hace y donde-, que es cuando nace
 * su contrasena.</p>
 *
 * <p><b>La baja del puesto es explicita (v3.4).</b> Asignar el puesto de
 * Responsable de una Coordinacion que ya lo tiene se rechaza: primero se da
 * de baja al vigente con {@code DELETE /{id}/asignaciones/{coordinacionId}},
 * que es lo que hace el boton "Dar de baja al responsable" de la pantalla de
 * Direcciones, y despues se nombra a su sucesor (RF-26).</p>
 *
 * <p>No existe ningun endpoint de eliminacion: un usuario solo se desactiva
 * (RN-09).</p>
 */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Personas del sistema, sus puestos y sus credenciales")
public class UsuarioController {

    private final GestionUsuariosServicio usuarios;

    public UsuarioController(GestionUsuariosServicio usuarios) {
        this.usuarios = usuarios;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /**
     * RF-28: una sola lista con todas las personas del sistema, acotada por
     * filtros. El Responsable queda limitado a su Coordinacion por el
     * servicio, envie lo que envie.
     *
     * @param sinAsignar TRUE deja solo a las personas registradas que aun no
     *                   tienen puesto: la tarea pendiente del Administrador
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Lista personas con busqueda, filtros y paginacion (RF-28)")
    public PaginaResponse<UsuarioDto> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Long coordinacionId,
            @RequestParam(required = false) Boolean sinAsignar,
            @RequestParam(required = false) EstadoCuenta estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "primerApellido") String ordenarPor,
            @RequestParam(defaultValue = "false") boolean descendente) {

        return PaginaResponse.de(usuarios.buscar(
                new FiltroUsuarios(q, rol, coordinacionId, sinAsignar, estado),
                CriterioPagina.de(pagina, tamano, ordenarPor, descendente)));
    }

    /** RF-29: todos los integrantes de una coordinacion en una sola seccion. */
    @GetMapping("/coordinacion/{coordinacionId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Integrantes de una coordinacion: responsable y operadores (RF-29)")
    public List<UsuarioDto> integrantes(@PathVariable Long coordinacionId,
                                        @RequestParam(defaultValue = "false") boolean soloActivos) {
        return usuarios.listarIntegrantes(coordinacionId, soloActivos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Obtiene una persona por identificador")
    public UsuarioDto obtener(@PathVariable Long id) {
        return usuarios.obtener(id);
    }

    // ------------------------------------------------------------------
    // Escritura
    // ------------------------------------------------------------------

    /**
     * RF-16b: registro previo. Deja a la persona en el sistema sin rol, sin
     * coordinacion y sin poder entrar; el puesto se le da despues.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Registra a una persona, sin puesto y sin credenciales (RF-16b)")
    public ResponseEntity<UsuarioDto> crear(@Valid @RequestBody UsuarioRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarios.crear(peticion.aComandoAlta()));
    }

    /**
     * RF-16e: registra a la persona y le da su puesto en un solo acto.
     *
     * <p>Es el alta que usa el Responsable con sus Operadores, donde el puesto
     * se sabe de antemano. Las dos operaciones comparten transaccion: hasta la
     * v3.8 el cliente las encadenaba, y si la segunda fallaba la persona
     * quedaba registrada sin puesto —invisible para el Responsable, que solo
     * ve a los suyos— con su DNI y su correo ya ocupados, de modo que ni podia
     * arreglarlo ni podia volver a intentarlo.</p>
     */
    @PostMapping("/con-puesto")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Registra a una persona y le asigna su puesto en una sola transaccion (RF-16e)")
    public ResponseEntity<AsignacionRealizadaDto> crearConPuesto(
            @Valid @RequestBody AltaConPuestoRequest peticion) {

        // El puesto viaja en el mismo comando que la asignacion normal, con el
        // identificador en null: la persona todavia no existe y es esta misma
        // operacion la que va a crearla.
        AsignacionRealizadaDto realizada = usuarios.registrarConPuesto(
                peticion.datos().aComandoAlta(),
                peticion.puesto().aComando(null));

        return ResponseEntity.status(HttpStatus.CREATED).body(realizada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Edita los datos personales de una persona (RF-21)")
    public UsuarioDto editar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest peticion) {
        return usuarios.editar(peticion.aComandoEdicion(id));
    }

    /**
     * RF-28d: le da a la persona un rol y su Coordinacion, si el rol es
     * operativo. Devuelve la contrasena temporal si acaba de nacer.
     *
     * <p>Se rechaza, nombrando al ocupante, si el puesto de Responsable de esa
     * Coordinacion ya esta dado (RN-06), y tambien si la persona ya tiene otra
     * coordinacion (RN-05).</p>
     */
    @PostMapping("/{id}/asignaciones")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Asigna un puesto a una persona registrada (RF-28d)")
    public AsignacionRealizadaDto asignar(@PathVariable Long id,
                                          @Valid @RequestBody AsignacionRequest peticion) {
        return usuarios.asignar(peticion.aComando(id));
    }

    /**
     * RF-26, RF-28d: da de baja el puesto de la persona en esa Coordinacion.
     *
     * <p>Es la baja del Responsable cuando quien lo ocupa es el Responsable
     * vigente, y solo el Administrador puede pedirla. La persona vuelve a
     * quedar registrada, sin puesto y con su cuenta activa.</p>
     */
    @DeleteMapping("/{id}/asignaciones/{coordinacionId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Da de baja el puesto de una persona en su coordinacion (RF-26, RF-28d)")
    public UsuarioDto retirar(@PathVariable Long id, @PathVariable Long coordinacionId) {
        return usuarios.retirarDe(id, coordinacionId);
    }

    /**
     * RF-22b: lleva la cuenta a uno de sus tres estados. Nunca la elimina
     * (RN-09).
     *
     * <p>La <b>suspension</b> es temporal y conserva el puesto; la <b>baja</b>
     * es la salida de la institucion y lo libera (RN-34). El Administrador lo
     * hace sobre cualquiera salvo sobre si mismo, y nunca sobre el ultimo
     * Administrador activo (RF-25); el Responsable, solo sobre los Operadores
     * de su Coordinacion (RF-29).</p>
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Activa, suspende o da de baja una cuenta. Nunca la elimina (RF-22b)")
    public UsuarioDto cambiarEstado(@PathVariable Long id,
                                    @Valid @RequestBody CambioEstadoCuentaRequest peticion) {
        return usuarios.cambiarEstado(id, peticion.estado());
    }

    // ------------------------------------------------------------------
    // RF-26b: cambio de Responsable de una Coordinacion
    // ------------------------------------------------------------------

    /**
     * RF-26b: quienes pueden tomar el puesto de Responsable de esa
     * Coordinacion (RN-35).
     */
    @GetMapping("/coordinacion/{coordinacionId}/candidatos-responsable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Personas que pueden hacerse cargo de una coordinacion (RF-26b)")
    public List<UsuarioDto> candidatosAResponsable(@PathVariable Long coordinacionId) {
        return usuarios.candidatosAResponsable(coordinacionId);
    }

    /**
     * RF-26b: pone a otra persona al frente de la Coordinacion.
     *
     * <p>El saliente queda libre —sin rol y sin Coordinacion— en la misma
     * transaccion, de modo que la Coordinacion no pasa ni un minuto sin
     * responsable (RF-26, RN-07).</p>
     */
    @PostMapping("/coordinacion/{coordinacionId}/responsable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambia el responsable de una coordinacion (RF-26b, RN-35)")
    public AsignacionRealizadaDto cambiarResponsable(@PathVariable Long coordinacionId,
                                                     @Valid @RequestBody CambioResponsableRequest peticion) {
        return usuarios.cambiarResponsable(coordinacionId, peticion.usuarioId());
    }

    @PostMapping("/{id}/restablecer-password")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Restablece la contrasena y fuerza el cambio en el siguiente ingreso (RF-06)")
    public PasswordTemporalDto restablecerPassword(@PathVariable Long id) {
        return usuarios.restablecerPassword(id);
    }

    @PostMapping("/{id}/desbloquear")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    @Operation(summary = "Libera el bloqueo temporal por intentos fallidos (RF-08)")
    public UsuarioDto desbloquear(@PathVariable Long id) {
        return usuarios.desbloquear(id);
    }
}
