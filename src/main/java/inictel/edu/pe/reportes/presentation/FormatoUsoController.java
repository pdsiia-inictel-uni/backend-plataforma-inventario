package inictel.edu.pe.reportes.presentation;

import inictel.edu.pe.reportes.application.dto.ArchivoExportadoDto;
import inictel.edu.pe.reportes.application.service.FormatoUsoServicio;
import inictel.edu.pe.reportes.presentation.dto.FormatoUsoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * Formato de registro de uso de equipos de investigacion (RF-78, RF-79).
 *
 * <p>Devuelve un PDF y nada mas. La peticion es un POST porque lleva cuerpo
 * —lo que se escribio a mano en el formulario—, no porque cree nada: no hay
 * fila que insertar, ni movimiento en el historial del bien, ni cambio en su
 * condicion (RN-36). Por eso responde 200 y no 201, y por eso no hay ningun
 * GET que devuelva "los formatos generados": no existen.</p>
 *
 * <p>El documento viaja <b>inline</b>: la pantalla lo enseña antes de que
 * nadie lo descargue, para que quien lo firma lo lea entero primero
 * (RF-79).</p>
 */
@RestController
@RequestMapping("/api/reportes/equipos")
@Tag(name = "Reportes - Formato de uso",
        description = "Formato de registro de uso de equipos de investigacion, en PDF")
public class FormatoUsoController {

    private final FormatoUsoServicio formatos;

    public FormatoUsoController(FormatoUsoServicio formatos) {
        this.formatos = formatos;
    }

    /**
     * RF-78: genera el formato de un bien concreto.
     *
     * <p>Exclusivo del Responsable, que es quien lo firma como coordinador y
     * quien responde por el equipo que sale. El servicio vuelve a comprobar el
     * rol y la Coordinacion del bien, de modo que la anotacion no es la unica
     * defensa (RN-23).</p>
     */
    @PostMapping("/{equipoId}/formato-uso")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Genera en PDF el formato de registro de uso de un equipo. "
            + "No guarda nada ni cambia la condicion del bien (RF-78, RN-36)")
    public ResponseEntity<byte[]> formatoDeUso(@PathVariable Long equipoId,
                                               @Valid @RequestBody FormatoUsoRequest peticion) {

        ArchivoExportadoDto archivo = formatos.generar(equipoId, peticion.aComando());

        ContentDisposition disposicion = ContentDisposition.inline()
                .filename(archivo.nombre(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(archivo.contenido());
    }
}
