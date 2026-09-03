package inictel.edu.pe.inventario.domain.service;

import java.io.InputStream;

/**
 * Puerto de almacenamiento de las fotografias de los bienes (RF-22).
 *
 * <p>El dominio sabe que un bien puede tener una foto identificada por una
 * URL; donde se guarda —disco local hoy, un servicio de objetos manana— es
 * decision de infraestructura.</p>
 */
public interface AlmacenFotos {

    /**
     * Guarda la imagen y devuelve la URL publica con la que se recupera.
     *
     * @param nombreOriginal nombre del archivo cargado, para deducir la extension
     * @param tipoContenido  tipo MIME informado por el cliente
     * @param contenido      flujo de bytes de la imagen
     */
    String guardar(String nombreOriginal, String tipoContenido, InputStream contenido);
}
