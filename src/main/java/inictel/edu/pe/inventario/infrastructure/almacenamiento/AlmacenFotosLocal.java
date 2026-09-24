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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Adaptador de {@link AlmacenFotos} sobre el sistema de archivos local
 * (RF-51). Las imagenes viven en la subcarpeta {@value #CARPETA} del
 * directorio de archivos ({@code app.archivos.directorio}); los documentos de
 * baja, en otra ({@link AlmacenDocumentosBajaLocal}).
 */
@Component
public class AlmacenFotosLocal implements AlmacenFotos {

    /** Subcarpeta de las fotografias, y segmento de su URL publica. */
    public static final String CARPETA = "imagenes";

    private static final Logger log = LoggerFactory.getLogger(AlmacenFotosLocal.class);
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("jpg", "jpeg", "png", "webp");
    private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

    private final Path raiz;
    private final Path directorio;
    /** Prefijo de las URLs actuales: {@code /api/archivos/imagenes}. */
    private final String urlPublica;
    /** Prefijo de las URLs anteriores a las subcarpetas: {@code /api/archivos}. */
    private final String urlAnterior;

    public AlmacenFotosLocal(AppProperties propiedades) {
        this.raiz = Paths.get(propiedades.archivos().directorio()).toAbsolutePath().normalize();
        this.directorio = raiz.resolve(CARPETA);
        this.urlAnterior = propiedades.archivos().urlPublica();
        this.urlPublica = urlAnterior + "/" + CARPETA;
        try {
            Files.createDirectories(directorio);
            log.info("Directorio de fotografías: {}", directorio);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de archivos: " + directorio, ex);
        }
        trasladarImagenesAnteriores();
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
            throw new DatosInvalidosException("archivo", "El nombre del archivo no es válido.");
        }
        try (contenido) {
            Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo guardar la fotografía.", ex);
        }
        return urlPublica + "/" + nombre;
    }

    /**
     * Borra del disco la fotografia sustituida (RF-51f).
     *
     * <p>Acepta las URLs actuales y las anteriores a las subcarpetas. Un fallo
     * al borrar se registra y no se propaga: la sustitucion ya quedo
     * confirmada y un archivo huerfano no justifica un error.</p>
     */
    @Override
    public void eliminar(String url) {
        String nombre = nombreDe(url);
        if (nombre == null) {
            return;
        }
        Path ruta = directorio.resolve(nombre).normalize();
        if (nombre.isBlank() || !ruta.startsWith(directorio) || ruta.equals(directorio)) {
            return;
        }
        try {
            if (Files.deleteIfExists(ruta)) {
                log.info("Fotografía sustituida eliminada: {}", nombre);
            }
        } catch (IOException ex) {
            log.warn("No se pudo eliminar la fotografía sustituida {}: {}", nombre, ex.getMessage());
        }
    }

    /** Ruta fisica de una fotografia ya almacenada, para servirla. */
    public Path resolver(String nombre) {
        Path ruta = directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio) || ruta.equals(directorio) || !Files.isRegularFile(ruta)) {
            throw new RecursoNoEncontradoException("El archivo solicitado no existe.");
        }
        return ruta;
    }

    private String nombreDe(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith(urlPublica + "/")) {
            return url.substring(urlPublica.length() + 1);
        }
        if (url.startsWith(urlAnterior + "/")) {
            String resto = url.substring(urlAnterior.length() + 1);
            return resto.contains("/") ? null : resto;
        }
        return null;
    }

    /**
     * Antes de las subcarpetas las imagenes se guardaban en la raiz del
     * directorio de archivos. Se trasladan una sola vez a {@code imagenes/},
     * y la ruta anterior {@code /api/archivos/{nombre}} sigue sirviendolas.
     * En una instalacion nueva no hay nada que trasladar.
     */
    private void trasladarImagenesAnteriores() {
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(raiz)) {
            for (Path archivo : archivos) {
                if (!Files.isRegularFile(archivo)
                        || !EXTENSIONES_PERMITIDAS.contains(extension(archivo.getFileName().toString()))) {
                    continue;
                }
                Path destino = directorio.resolve(archivo.getFileName());
                if (!Files.exists(destino)) {
                    Files.move(archivo, destino);
                    log.info("Fotografía trasladada a {}/: {}", CARPETA, archivo.getFileName());
                }
            }
        } catch (IOException ex) {
            log.warn("No se pudieron trasladar las fotografías anteriores: {}", ex.getMessage());
        }
    }

    private static String extension(String nombreOriginal) {
        if (nombreOriginal == null) {
            return "";
        }
        int punto = nombreOriginal.lastIndexOf('.');
        return punto < 0 ? "" : nombreOriginal.substring(punto + 1).toLowerCase(Locale.ROOT);
    }
}
