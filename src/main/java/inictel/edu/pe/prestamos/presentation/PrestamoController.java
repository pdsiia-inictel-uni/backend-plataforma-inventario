package inictel.edu.pe.prestamos.presentation;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.presentation.PaginaResponse;
import inictel.edu.pe.prestamos.application.dto.PrestamoDto;
import inictel.edu.pe.prestamos.application.service.GestionPrestamosServicio;
import inictel.edu.pe.prestamos.domain.model.EstadoPrestamo;
import inictel.edu.pe.prestamos.domain.repository.FiltroPrestamos;
import inictel.edu.pe.prestamos.presentation.dto.DevolucionRequest;
import inictel.edu.pe.prestamos.presentation.dto.PrestamoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Prestamos y devoluciones (RF-58 .. RF-68).
 *
 * <p>Los registran el Responsable y el Operador de la coordinacion duenna del
 * bien. El Administrador consulta pero no mueve bienes (RN-22).</p>
 */
@RestController
@RequestMapping("/api/prestamos")
@Tag(name = "Prestamos", description = "Salida y retorno de bienes con trazabilidad")
public class PrestamoController {

    private final GestionPrestamosServicio prestamos;

    public PrestamoController(GestionPrestamosServicio prestamos) {
        this.prestamos = prestamos;
    }

    @GetMapping
    @Operation(summary = "Lista prestamos con filtros y paginacion (RF-66, RF-67)")
    public PaginaResponse<PrestamoDto> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long coordinacionId,
            @RequestParam(required = false) EstadoPrestamo estado,
            @RequestParam(required = false) Long equipoId,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Boolean vencidos,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "fechaPrestamo") String ordenarPor,
            @RequestParam(defaultValue = "true") boolean descendente) {

        FiltroPrestamos filtro = new FiltroPrestamos(
                coordinacionId, q, estado, equipoId, dni, desde, hasta, vencidos);
        return PaginaResponse.de(
                prestamos.buscar(filtro, CriterioPagina.de(pagina, tamano, ordenarPor, descendente)));
    }

    @GetMapping("/vencidos")
    @Operation(summary = "Prestamos activos que superaron la fecha estimada de devolucion (RF-67)")
    public List<PrestamoDto> vencidos(@RequestParam(required = false) Long coordinacionId) {
        return prestamos.vencidos(coordinacionId);
    }

    @GetMapping("/bien/{equipoId}")
    @Operation(summary = "Historial de prestamos de un bien (RF-66)")
    public List<PrestamoDto> historialPorBien(@PathVariable Long equipoId) {
        return prestamos.historialPorBien(equipoId);
    }

    @GetMapping("/persona/{dni}")
    @Operation(summary = "Historial de prestamos de una persona por DNI (RF-66)")
    public List<PrestamoDto> historialPorPersona(@PathVariable String dni,
                                                 @RequestParam(required = false) Long coordinacionId) {
        return prestamos.historialPorPersona(dni, coordinacionId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un prestamo por identificador")
    public PrestamoDto obtener(@PathVariable Long id) {
        return prestamos.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','OPERADOR')")
    @Operation(summary = "Registra el prestamo de un bien disponible (RF-59, RF-60)")
    public ResponseEntity<PrestamoDto> registrar(@Valid @RequestBody PrestamoRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamos.registrar(peticion.aComando()));
    }

    @PostMapping("/{id}/devolucion")
    @PreAuthorize("hasAnyRole('RESPONSABLE','OPERADOR')")
    @Operation(summary = "Registra la devolucion con checklist de conformidad (RF-61 .. RF-65)")
    public PrestamoDto devolver(@PathVariable Long id, @Valid @RequestBody DevolucionRequest peticion) {
        return prestamos.registrarDevolucion(peticion.aComando(id));
    }
}
