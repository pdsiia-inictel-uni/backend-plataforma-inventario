package inictel.edu.pe.inventario.infrastructure.almacenamiento;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.infrastructure.config.AppProperties;
import inictel.edu.pe.inventario.domain.service.AlmacenDocumentosBaja;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Adaptador de {@link AlmacenDocumentosBaja} sobre el sistema de archivos
 * local (RF-42). Los PDF viven en la subcarpeta {@value #CARPETA} del
 * directorio de archivos, separados de las imagenes.
 */
@Component
public class AlmacenDocumentosBajaLocal implements AlmacenDocumentosBaja {

    /** Subcarpeta de los documentos de baja, y segmento de su URL publica. */
    public static final String CARPETA = "archivos";

    /** 5 MB, el mismo limite que las fotografias (RF-42, RF-51e). */
    public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;

    private static final Logger log = LoggerFactory.getLogger(AlmacenDocumentosBajaLocal.class);
    private static final List<String> TIPOS_PERMITIDOS = List.of("application/pdf", "application/x-pdf");
    private static final byte[] CABECERA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final Path directorio;
    private final String urlPublica;

    public AlmacenDocumentosBajaLocal(AppProperties propiedades) {
        this.directorio = Paths.get(propiedades.archivos().directorio()).toAbsolutePath().normalize()
                .resolve(CARPETA);
        this.urlPublica = propiedades.archivos().urlPublica() + "/" + CARPETA;
        try {
            Files.createDirectories(directorio);
            log.info("Directorio de documentos de baja: {}", directorio);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de documentos: " + directorio, ex);
        }
    }

    @Override
    public String guardar(String nombreOriginal, String tipoContenido, long tamano, InputStream contenido) {
        if (contenido == null || tamano <= 0) {
            throw new DatosInvalidosException("archivo", "Adjunte el documento de baja en PDF.");
        }
        if (tamano > TAMANO_MAXIMO) {
            throw new DatosInvalidosException("archivo", "El PDF no puede superar los 5 MB.");
        }
        boolean tipoValido = tipoContenido != null
                && TIPOS_PERMITIDOS.contains(tipoContenido.toLowerCase(Locale.ROOT));
        boolean extensionValida = nombreOriginal != null
                && nombreOriginal.toLowerCase(Locale.ROOT).endsWith(".pdf");
        if (!tipoValido || !extensionValida) {
            throw new DatosInvalidosException("archivo", "Solo se admite un archivo PDF.");
        }

        String nombre = UUID.randomUUID() + ".pdf";
        Path destino = directorio.resolve(nombre).normalize();
        try (InputStream entrada = new BufferedInputStream(contenido)) {
            // El nombre y el tipo los declara el cliente: se comprueba que el
            // contenido empiece de verdad como un PDF.
            entrada.mark(CABECERA_PDF.length);
            byte[] cabecera = entrada.readNBytes(CABECERA_PDF.length);
            if (!Arrays.equals(cabecera, CABECERA_PDF)) {
                throw new DatosInvalidosException("archivo", "El archivo no es un PDF válido.");
            }
            entrada.reset();
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar el documento de baja.", ex);
        }
        return urlPublica + "/" + nombre;
    }

    @Override
    public void eliminar(String url) {
        String prefijo = urlPublica + "/";
        if (url == null || !url.startsWith(prefijo)) {
            return;
        }
        String nombre = url.substring(prefijo.length());
        Path ruta = directorio.resolve(nombre).normalize();
        if (nombre.isBlank() || !ruta.startsWith(directorio) || ruta.equals(directorio)) {
            return;
        }
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException ex) {
            log.warn("No se pudo eliminar el documento de baja {}: {}", nombre, ex.getMessage());
        }
    }

    /** Ruta fisica de un documento ya almacenado, para servirlo. */
    public Path resolver(String nombre) {
        Path ruta = directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio) || ruta.equals(directorio) || !Files.isRegularFile(ruta)) {
            throw new RecursoNoEncontradoException("El archivo solicitado no existe.");
        }
        return ruta;
    }
}
