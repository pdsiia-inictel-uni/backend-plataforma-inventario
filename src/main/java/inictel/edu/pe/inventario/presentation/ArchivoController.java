package inictel.edu.pe.inventario.presentation;

import inictel.edu.pe.inventario.infrastructure.almacenamiento.AlmacenFotosLocal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Entrega de las fotografias de los bienes (RF-22).
 *
 * <p>Requiere token JWT como cualquier otro endpoint (criterio de aceptacion,
 * seccion 7); el frontend las descarga y las muestra como blob.</p>
 */
@RestController
@RequestMapping("/api/archivos")
@Tag(name = "Inventario - Archivos", description = "Fotografias de los bienes")
public class ArchivoController {

    private final AlmacenFotosLocal almacen;

    public ArchivoController(AlmacenFotosLocal almacen) {
        this.almacen = almacen;
    }

    @GetMapping("/{nombre}")
    @Operation(summary = "Descarga una fotografia previamente cargada")
    public ResponseEntity<Resource> descargar(@PathVariable String nombre) {
        Path ruta = almacen.resolver(nombre);
        try {
            String tipo = Files.probeContentType(ruta);
            MediaType mediaType = tipo != null ? MediaType.parseMediaType(tipo) : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                    .body(new FileSystemResource(ruta));
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer la fotografia solicitada.", ex);
        }
    }
}
