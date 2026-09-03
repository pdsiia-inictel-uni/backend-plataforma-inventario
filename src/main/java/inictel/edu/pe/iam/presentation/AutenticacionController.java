package inictel.edu.pe.iam.presentation;

import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.iam.application.dto.SesionDto;
import inictel.edu.pe.iam.application.dto.UsuarioDto;
import inictel.edu.pe.iam.application.service.AutenticacionServicio;
import inictel.edu.pe.iam.presentation.dto.CambioPasswordRequest;
import inictel.edu.pe.iam.presentation.dto.CambioUsuarioRequest;
import inictel.edu.pe.iam.presentation.dto.LoginRequest;
import inictel.edu.pe.iam.presentation.dto.PrimerIngresoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Acceso al sistema: inicio y cierre de sesion, perfil propio y cambio de las
 * credenciales propias (RF-01 .. RF-06).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "IAM - Autenticacion", description = "Inicio de sesion, perfil y credenciales propias")
public class AutenticacionController {

    private final AutenticacionServicio autenticacion;
    private final ContextoUsuario contexto;

    public AutenticacionController(AutenticacionServicio autenticacion, ContextoUsuario contexto) {
        this.autenticacion = autenticacion;
        this.contexto = contexto;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Inicia sesion y devuelve el token de acceso (RF-01, RF-02)")
    public ResponseEntity<SesionDto> login(@Valid @RequestBody LoginRequest peticion) {
        return ResponseEntity.ok(autenticacion.iniciarSesion(peticion.aComando()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cierra la sesion del usuario autenticado (RF-03)")
    public ResponseEntity<Map<String, String>> logout() {
        // El token es sin estado: cerrarlo es descartarlo en el cliente. La
        // llamada existe para que el frontend tenga un punto unico de cierre.
        return ResponseEntity.ok(Map.of("mensaje", "Sesion cerrada correctamente."));
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado")
    public ResponseEntity<UsuarioDto> perfil() {
        return ResponseEntity.ok(autenticacion.perfil(contexto.requerido().id()));
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambia la propia contrasena y renueva la sesion (RF-06, RNF-05)")
    public ResponseEntity<SesionDto> cambiarPassword(@Valid @RequestBody CambioPasswordRequest peticion) {
        return ResponseEntity.ok(
                autenticacion.cambiarPasswordPropia(peticion.aComando(contexto.requerido().id())));
    }

    @PostMapping("/primer-ingreso")
    // RF-21: los datos personales de una persona los administra el Administrador.
    // Este endpoint es la unica excepcion y solo para el propio Administrador,
    // cuya cuenta inicial nace con un nombre y un DNI de relleno.
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Primer ingreso del Administrador: confirma sus datos y define su contrasena (RF-06b)")
    public ResponseEntity<SesionDto> primerIngreso(@Valid @RequestBody PrimerIngresoRequest peticion) {
        return ResponseEntity.ok(
                autenticacion.completarPrimerIngreso(peticion.aComando(contexto.requerido().id())));
    }

    @PutMapping("/mi-usuario")
    @Operation(summary = "Cambia el propio nombre de usuario y correo institucional")
    public ResponseEntity<SesionDto> cambiarUsuario(@Valid @RequestBody CambioUsuarioRequest peticion) {
        return ResponseEntity.ok(
                autenticacion.cambiarCredencialesPropias(peticion.aComando(contexto.requerido().id())));
    }
}
