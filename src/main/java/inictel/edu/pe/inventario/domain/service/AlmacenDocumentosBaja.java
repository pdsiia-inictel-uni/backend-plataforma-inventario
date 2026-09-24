package inictel.edu.pe.inventario.domain.service;

import java.io.InputStream;

/**
 * Puerto de almacenamiento del documento que sustenta la baja de un bien
 * (RF-42): un PDF, obligatorio.
 *
 * <p>Como con las fotografias ({@link AlmacenFotos}), el dominio solo conoce
 * la URL con la que se recupera; donde se guarda el archivo es decision de
 * infraestructura. Los documentos de baja viven en su propia carpeta, separada
 * de la de imagenes.</p>
 */
public interface AlmacenDocumentosBaja {

    /**
     * Guarda el PDF y devuelve la URL con la que se recupera.
     *
     * @param nombreOriginal nombre del archivo cargado
     * @param tipoContenido  tipo MIME informado por el cliente
     * @param tamano         tamano en bytes
     * @param contenido      flujo de bytes del documento
     */
    String guardar(String nombreOriginal, String tipoContenido, long tamano, InputStream contenido);

    /** Elimina el documento; una URL desconocida no es un error. */
    void eliminar(String url);
}
