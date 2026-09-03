package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.Equipo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fila del listado de inventario (RF-47, RF-50).
 *
 * <p>Solo lo que la tabla muestra: mantener la proyeccion estrecha es lo que
 * permite paginar 50,000 bienes sin degradar la respuesta (RNF-12).</p>
 */
public record EquipoResumenDto(
        Long id,
        String nombre,
        String marca,
        String modelo,
        String numeroSerie,
        String codigoInventario,
        String codigoPatrimonial,
        String categoria,
        String laboratorio,
        CondicionEquipo condicion,
        String condicionEtiqueta,
        LocalDate fechaAdquisicion,
        BigDecimal costo,
        boolean revisionPendiente,
        LocalDateTime fechaRegistro) {

    public static EquipoResumenDto de(Equipo e, String laboratorio) {
        return new EquipoResumenDto(
                e.getId(),
                e.getNombre(),
                e.getMarca(),
                e.getModelo(),
                e.getNumeroSerie().valor(),
                e.getCodigoInventario().valor(),
                e.getCodigoPatrimonial().valor(),
                e.getCategoria() == null ? null : e.getCategoria().nombre(),
                laboratorio,
                e.getCondicion(),
                e.getCondicion().getEtiqueta(),
                e.getFechaAdquisicion(),
                e.getCosto(),
                e.isRevisionPendiente(),
                e.getFechaRegistro());
    }
}
