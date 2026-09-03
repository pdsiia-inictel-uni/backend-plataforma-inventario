package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;
import inictel.edu.pe.inventario.domain.model.TipoMovimiento;

import java.time.LocalDateTime;

/**
 * Un hecho de la linea de tiempo del bien, listo para leerse (RF-56).
 *
 * <p>{@code resumen} trae la frase ya redactada —"Prestado por Ana Diaz
 * (Operador)"— para que la vista no tenga que componerla y el texto sea el
 * mismo en la pantalla, en la exportacion y en cualquier otro consumidor.</p>
 */
public record MovimientoDto(
        Long id,
        TipoMovimiento tipo,
        String tipoEtiqueta,
        String condicionAnterior,
        String condicionNueva,
        String detalle,
        String usuario,
        Rol rolUsuario,
        String rolEtiqueta,
        String resumen,
        LocalDateTime fechaHora) {

    public static MovimientoDto de(MovimientoEquipo m) {
        String autor = m.usuarioNombre() == null ? "Usuario no disponible" : m.usuarioNombre();
        String rol = m.rolUsuario() == null ? null : m.rolUsuario().getEtiqueta();
        String resumen = rol == null
                ? m.tipo().getEtiqueta() + " por " + autor
                : m.tipo().getEtiqueta() + " por " + autor + " (" + rol + ")";

        return new MovimientoDto(
                m.id(),
                m.tipo(),
                m.tipo().getEtiqueta(),
                m.condicionAnterior() == null ? null : m.condicionAnterior().getEtiqueta(),
                m.condicionNueva() == null ? null : m.condicionNueva().getEtiqueta(),
                m.detalle(),
                autor,
                m.rolUsuario(),
                rol,
                resumen,
                m.fechaHora());
    }
}
