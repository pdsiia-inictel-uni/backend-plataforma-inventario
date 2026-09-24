package inictel.edu.pe.inventario.presentation;

import inictel.edu.pe.inventario.infrastructure.almacenamiento.AlmacenDocumentosBajaLocal;
import inictel.edu.pe.inventario.infrastructure.almacenamiento.AlmacenFotosLocal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
 * Entrega de los archivos de los bienes: fotografias (RF-51) y documentos de
 * baja (RF-42), cada tipo en su carpeta.
 *
 * <p>Requiere token JWT como cualquier otro endpoint; el frontend los descarga
 * y los muestra como blob.</p>
 */
@RestController
@RequestMapping("/api/archivos")
@Tag(name = "Inventario - Archivos", description = "Fotografias y documentos de baja de los bienes")
public class ArchivoController {

    private final AlmacenFotosLocal fotos;
    private final AlmacenDocumentosBajaLocal documentos;

    public ArchivoController(AlmacenFotosLocal fotos, AlmacenDocumentosBajaLocal documentos) {
        this.fotos = fotos;
        this.documentos = documentos;
    }

    @GetMapping("/" + AlmacenFotosLocal.CARPETA + "/{nombre}")
    @Operation(summary = "Descarga una fotografia de un bien")
    public ResponseEntity<Resource> imagen(@PathVariable String nombre) {
        return servir(fotos.resolver(nombre));
    }

    @GetMapping("/" + AlmacenDocumentosBajaLocal.CARPETA + "/{nombre}")
    @Operation(summary = "Descarga el documento PDF que sustenta la baja de un bien")
    public ResponseEntity<Resource> documentoBaja(@PathVariable String nombre) {
        Path ruta = documentos.resolver(nombre);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(nombre).build().toString())
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(new FileSystemResource(ruta));
    }

    /** Ruta anterior a las subcarpetas: sirve las fotografias con su URL antigua. */
    @GetMapping("/{nombre}")
    @Operation(summary = "Descarga una fotografia por su URL anterior (sin subcarpeta)")
    public ResponseEntity<Resource> imagenAnterior(@PathVariable String nombre) {
        return servir(fotos.resolver(nombre));
    }

    private ResponseEntity<Resource> servir(Path ruta) {
        try {
            String tipo = Files.probeContentType(ruta);
            MediaType mediaType = tipo != null ? MediaType.parseMediaType(tipo) : MediaType.APPLICATION_OCTET_STREAM;
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                    .body(new FileSystemResource(ruta));
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer el archivo solicitado.", ex);
        }
    }
}
