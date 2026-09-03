package inictel.edu.pe.inventario.infrastructure.almacenamiento;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.infrastructure.config.AppProperties;
import inictel.edu.pe.inventario.domain.service.AlmacenFotos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Adaptador de {@link AlmacenFotos} sobre el sistema de archivos local
 * (RF-22). El directorio se configura con {@code app.archivos.directorio}.
 */
@Component
public class AlmacenFotosLocal implements AlmacenFotos {

    private static final Logger log = LoggerFactory.getLogger(AlmacenFotosLocal.class);
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("jpg", "jpeg", "png", "webp");
    private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

    private final Path directorio;
    private final String urlPublica;

    public AlmacenFotosLocal(AppProperties propiedades) {
        this.directorio = Paths.get(propiedades.archivos().directorio()).toAbsolutePath().normalize();
        this.urlPublica = propiedades.archivos().urlPublica();
        try {
            Files.createDirectories(directorio);
            log.info("Directorio de fotografias: {}", directorio);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de archivos: " + directorio, ex);
        }
    }

    @Override
    public String guardar(String nombreOriginal, String tipoContenido, InputStream contenido) {
        if (contenido == null) {
            throw new DatosInvalidosException("archivo", "Seleccione una imagen.");
        }
        if (tipoContenido == null || !TIPOS_PERMITIDOS.contains(tipoContenido.toLowerCase(Locale.ROOT))) {
            throw new DatosInvalidosException("archivo", "Formato no admitido. Use imagenes JPG, PNG o WEBP.");
        }
        String extension = extension(nombreOriginal);
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new DatosInvalidosException("archivo",
                    "Extension no admitida. Use archivos .jpg, .jpeg, .png o .webp.");
        }

        String nombre = UUID.randomUUID() + "." + extension;
        Path destino = directorio.resolve(nombre).normalize();
        if (!destino.startsWith(directorio)) {
            throw new DatosInvalidosException("archivo", "El nombre del archivo no es valido.");
        }
        try (contenido) {
            Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la fotografia.", ex);
        }
        return urlPublica + "/" + nombre;
    }

    /** Ruta fisica de una fotografia ya almacenada, para servirla. */
    public Path resolver(String nombre) {
        Path ruta = directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio) || !Files.exists(ruta)) {
            throw new RecursoNoEncontradoException("El archivo solicitado no existe.");
        }
        return ruta;
    }

    private String extension(String nombreOriginal) {
        if (nombreOriginal == null) {
            return "";
        }
        int punto = nombreOriginal.lastIndexOf('.');
        return punto < 0 ? "" : nombreOriginal.substring(punto + 1).toLowerCase(Locale.ROOT);
    }
}
