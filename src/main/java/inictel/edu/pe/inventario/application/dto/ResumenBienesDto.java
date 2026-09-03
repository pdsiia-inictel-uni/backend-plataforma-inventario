package inictel.edu.pe.inventario.application.dto;

import java.util.Map;

/**
 * Cifras del inventario que consume el panel de control (RF-75 .. RF-77).
 *
 * <p>Cuando se pide sin coordinacion abarca toda la institucion (vista del
 * Administrador); con coordinacion, solo la suya.</p>
 */
public record ResumenBienesDto(
        long total,
        long operativos,
        long prestados,
        long enMantenimiento,
        long dadosDeBaja,
        long revisionPendiente,
        Map<String, Long> porCategoria) {
}
