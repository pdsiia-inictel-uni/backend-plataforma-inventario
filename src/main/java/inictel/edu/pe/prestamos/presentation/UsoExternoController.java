package inictel.edu.pe.prestamos.presentation;

import inictel.edu.pe.prestamos.application.dto.UsoExternoDto;
import inictel.edu.pe.prestamos.application.service.GestionUsosExternosServicio;
import inictel.edu.pe.prestamos.presentation.dto.AbrirUsoExternoRequest;
import inictel.edu.pe.prestamos.presentation.dto.CerrarUsoExternoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Registro de uso externo de un equipo (RF-78): prestamo para que lo use,
 * dentro de la institucion, personal de otra institucion con su encargado.
 * Se guarda en dos partes —apertura y cierre— y queda como historia del bien.
 * El PDF para firmar lo genera {@code /api/reportes/usos-externos/{id}/formato}.
 */
@RestController
@RequestMapping("/api/usos-externos")
@Tag(name = "Usos externos", description = "Formato de registro de uso de equipos, guardado en dos partes")
public class UsoExternoController {

    private final GestionUsosExternosServicio usos;

    public UsoExternoController(GestionUsosExternosServicio usos) {
        this.usos = usos;
    }

    @GetMapping("/bien/{equipoId}")
    @Operation(summary = "Historial de usos externos de un equipo, el más reciente primero")
    public List<UsoExternoDto> historialPorBien(@PathVariable Long equipoId) {
        return usos.historialPorBien(equipoId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Un registro de uso externo")
    public UsoExternoDto obtener(@PathVariable Long id) {
        return usos.obtener(id);
    }

    @PostMapping("/bien/{equipoId}")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Primera parte (puntos 1 a 5): abre el uso y el equipo pasa a Prestado")
    public ResponseEntity<UsoExternoDto> abrir(@PathVariable Long equipoId,
                                               @Valid @RequestBody AbrirUsoExternoRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usos.abrir(equipoId, peticion.aComando()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Anula un uso con solo su primera parte: se borra y el equipo vuelve a Operativo")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        usos.anular(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cierre")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Parte final (puntos 6 a 10): cierra el uso y el equipo vuelve al servicio")
    public UsoExternoDto cerrar(@PathVariable Long id, @Valid @RequestBody CerrarUsoExternoRequest peticion) {
        return usos.cerrar(id, peticion.aComando());
    }
}
