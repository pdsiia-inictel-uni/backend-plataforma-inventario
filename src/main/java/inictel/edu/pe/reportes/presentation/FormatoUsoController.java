package inictel.edu.pe.reportes.presentation;

import inictel.edu.pe.reportes.application.dto.ArchivoExportadoDto;
import inictel.edu.pe.reportes.application.service.FormatoUsoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * Formato de registro de uso de equipos de investigacion, en PDF (RF-78).
 *
 * <p>Se genera a partir de un registro de uso externo guardado. Viaja
 * <b>inline</b>: la pantalla lo enseña antes de descargarlo.</p>
 */
@RestController
@RequestMapping("/api/reportes/usos-externos")
@Tag(name = "Reportes - Formato de uso",
        description = "Formato de registro de uso de equipos de investigacion, en PDF")
public class FormatoUsoController {

    private final FormatoUsoServicio formatos;

    public FormatoUsoController(FormatoUsoServicio formatos) {
        this.formatos = formatos;
    }

    @GetMapping("/{usoId}/formato")
    @Operation(summary = "PDF del formato de registro de uso de un uso externo guardado (RF-78)")
    public ResponseEntity<byte[]> formato(@PathVariable Long usoId) {
        ArchivoExportadoDto archivo = formatos.generar(usoId);

        ContentDisposition disposicion = ContentDisposition.inline()
                .filename(archivo.nombre(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(archivo.contenido());
    }
}
