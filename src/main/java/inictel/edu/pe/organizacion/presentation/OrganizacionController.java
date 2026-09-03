package inictel.edu.pe.organizacion.presentation;

import inictel.edu.pe.organizacion.application.dto.CoordinacionDto;
import inictel.edu.pe.organizacion.application.dto.DireccionDto;
import inictel.edu.pe.organizacion.application.dto.EstructuraDto;
import inictel.edu.pe.organizacion.application.dto.LaboratorioDto;
import inictel.edu.pe.organizacion.application.service.GestionOrganizacionServicio;
import inictel.edu.pe.organizacion.presentation.dto.CambioEstadoRequest;
import inictel.edu.pe.organizacion.presentation.dto.CoordinacionRequest;
import inictel.edu.pe.organizacion.presentation.dto.DireccionRequest;
import inictel.edu.pe.organizacion.presentation.dto.LaboratorioRequest;
import inictel.edu.pe.organizacion.presentation.dto.NuevaCoordinacionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Estructura organizacional: Direcciones, Coordinaciones y Laboratorios
 * (RF-09 .. RF-15).
 *
 * <p>Toda la escritura es exclusiva del Administrador. La lectura del catalogo
 * de coordinaciones y laboratorios queda abierta a los usuarios autenticados
 * porque los formularios de inventario la necesitan para poblar sus
 * selectores; los datos que expone son puramente estructurales.</p>
 */
@RestController
@RequestMapping("/api/organizacion")
@Tag(name = "Organizacion", description = "Direcciones, coordinaciones y laboratorios de INICTEL-UNI")
public class OrganizacionController {

    private final GestionOrganizacionServicio organizacion;

    public OrganizacionController(GestionOrganizacionServicio organizacion) {
        this.organizacion = organizacion;
    }

    // ------------------------------------------------------------------
    // Estructura completa (RF-15)
    // ------------------------------------------------------------------

    @GetMapping("/estructura")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Arbol institucional con el resumen de cada coordinacion (RF-15)")
    public EstructuraDto estructura(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return organizacion.estructura(soloActivas);
    }

    // ------------------------------------------------------------------
    // Direcciones (RF-10)
    // ------------------------------------------------------------------

    // RF-10: las direcciones de la institucion son fijas y se precargan con el
    // esquema (migracion V4). No hay alta ni desactivacion: no existe endpoint
    // que las cree ni que las apague, solo la lectura y la correccion de sus
    // datos.
    @GetMapping("/direcciones")
    @Operation(summary = "Lista las direcciones institucionales (RF-10)")
    public List<DireccionDto> listarDirecciones(@RequestParam(defaultValue = "true") boolean soloActivas) {
        return organizacion.listarDirecciones(soloActivas);
    }

    @PutMapping("/direcciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Corrige los datos de una direccion. Solo Administrador (RF-10)")
    public DireccionDto editarDireccion(@PathVariable Long id, @Valid @RequestBody DireccionRequest peticion) {
        return organizacion.editarDireccion(id, peticion.nombre(), peticion.sigla(), peticion.descripcion());
    }

    // ------------------------------------------------------------------
    // Coordinaciones (RF-11, RF-13)
    // ------------------------------------------------------------------

    @GetMapping("/coordinaciones")
    @Operation(summary = "Lista las coordinaciones, opcionalmente de una direccion")
    public List<CoordinacionDto> listarCoordinaciones(@RequestParam(required = false) Long direccionId,
                                                      @RequestParam(defaultValue = "true") boolean soloActivas) {
        return organizacion.listarCoordinaciones(direccionId, soloActivas);
    }

    @GetMapping("/coordinaciones/{id}")
    @Operation(summary = "Detalle de una coordinacion con su resumen")
    public CoordinacionDto obtenerCoordinacion(@PathVariable Long id) {
        return organizacion.obtenerCoordinacion(id);
    }

    @PostMapping("/coordinaciones")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea una coordinacion con su primer laboratorio. Solo Administrador (RF-11, RN-26)")
    public ResponseEntity<CoordinacionDto> crearCoordinacion(@Valid @RequestBody NuevaCoordinacionRequest peticion) {
        CoordinacionDto creada = organizacion.crearCoordinacion(
                peticion.direccionId(), peticion.nombre(), peticion.descripcion(),
                peticion.laboratorioNombre(), peticion.laboratorioUbicacion());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/coordinaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Edita una coordinacion. Solo Administrador")
    public CoordinacionDto editarCoordinacion(@PathVariable Long id,
                                              @Valid @RequestBody CoordinacionRequest peticion) {
        return organizacion.editarCoordinacion(id, peticion.nombre(), peticion.descripcion());
    }

    @PatchMapping("/coordinaciones/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activa o desactiva una coordinacion. Falla si tiene bienes o prestamos vivos (RF-13)")
    public CoordinacionDto cambiarEstadoCoordinacion(@PathVariable Long id,
                                                     @Valid @RequestBody CambioEstadoRequest peticion) {
        return organizacion.cambiarEstadoCoordinacion(id, peticion.activo());
    }

    // ------------------------------------------------------------------
    // Laboratorios (RF-12, RF-14)
    // ------------------------------------------------------------------

    @GetMapping("/coordinaciones/{coordinacionId}/laboratorios")
    @Operation(summary = "Lista los laboratorios de una coordinacion")
    public List<LaboratorioDto> listarLaboratorios(@PathVariable Long coordinacionId,
                                                   @RequestParam(defaultValue = "true") boolean soloActivos) {
        return organizacion.listarLaboratorios(coordinacionId, soloActivos);
    }

    @PostMapping("/laboratorios")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea un laboratorio. Solo Administrador (RF-12)")
    public ResponseEntity<LaboratorioDto> crearLaboratorio(@Valid @RequestBody LaboratorioRequest peticion) {
        LaboratorioDto creado = organizacion.crearLaboratorio(
                peticion.coordinacionId(), peticion.nombre(), peticion.ubicacion());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/laboratorios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Edita un laboratorio. Solo Administrador")
    public LaboratorioDto editarLaboratorio(@PathVariable Long id,
                                            @Valid @RequestBody LaboratorioRequest peticion) {
        return organizacion.editarLaboratorio(id, peticion.nombre(), peticion.ubicacion());
    }

    @PatchMapping("/laboratorios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activa o desactiva un laboratorio. Falla si tiene bienes ubicados (RF-14)")
    public LaboratorioDto cambiarEstadoLaboratorio(@PathVariable Long id,
                                                   @Valid @RequestBody CambioEstadoRequest peticion) {
        return organizacion.cambiarEstadoLaboratorio(id, peticion.activo());
    }
}
