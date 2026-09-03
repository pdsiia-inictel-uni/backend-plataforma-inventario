package inictel.edu.pe.reportes.presentation;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.repository.FiltroEquipos;
import inictel.edu.pe.reportes.application.dto.ArchivoExportadoDto;
import inictel.edu.pe.reportes.application.service.ExportacionInventarioServicio;
import inictel.edu.pe.reportes.domain.model.FormatoExportacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * Exportacion del inventario filtrado (RF-52).
 *
 * <p>Disponible para los tres roles: el Administrador exporta la Coordinacion
 * que elija, el Responsable y el Operador la suya. El servicio impone el
 * ambito, no el cliente (RN-23).</p>
 */
@RestController
@RequestMapping("/api/reportes/inventario")
@Tag(name = "Reportes - Exportacion", description = "Descarga del inventario en Excel, CSV y PDF")
public class ExportacionController {

    private final ExportacionInventarioServicio exportacion;

    public ExportacionController(ExportacionInventarioServicio exportacion) {
        this.exportacion = exportacion;
    }

    @GetMapping("/{formato}")
    @Operation(summary = "Exporta el inventario filtrado en el formato indicado: xlsx, csv o pdf (RF-52)")
    public ResponseEntity<byte[]> exportarInventario(
            @PathVariable String formato,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long coordinacionId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long laboratorioId,
            @RequestParam(required = false) CondicionEquipo condicion,
            @RequestParam(defaultValue = "true") boolean todas,
            @RequestParam(required = false) Long responsableEquipoId,
            @RequestParam(defaultValue = "false") boolean sinResponsable) {

        // RF-84: el reporte sale con el mismo recorte que la pantalla. Exportar
        // el inventario entero cuando lo que se esta mirando son los equipos de
        // un operador convierte la descarga en otro documento distinto del que
        // se pidio.
        FiltroEquipos filtro = new FiltroEquipos(
                coordinacionId, q, categoriaId, laboratorioId, condicion, todas,
                responsableEquipoId, sinResponsable);

        ArchivoExportadoDto archivo = exportacion.exportar(FormatoExportacion.desde(formato), filtro);

        ContentDisposition disposicion = ContentDisposition.attachment()
                .filename(archivo.nombre(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(archivo.contenido());
    }
}
