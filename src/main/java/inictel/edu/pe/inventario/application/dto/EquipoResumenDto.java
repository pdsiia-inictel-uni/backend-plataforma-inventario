package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.Equipo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fila del listado de inventario (RF-47, RF-50, RF-84).
 *
 * <p>Solo lo que la tabla muestra: mantener la proyeccion estrecha es lo que
 * permite paginar 50,000 bienes sin degradar la respuesta (RNF-12).</p>
 *
 * @param responsableEquipoId Operador a cargo del bien, o {@code null} si lo
 *                            lleva el Responsable de la Coordinacion (RF-83)
 * @param responsableEquipo   nombre de quien responde por el bien hoy. Nunca
 *                            queda vacio: si no hay Operador asignado es el
 *                            Responsable vigente, que es lo que significa no
 *                            tener asignacion (RN-37)
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
        Long responsableEquipoId,
        String responsableEquipo,
        LocalDateTime fechaRegistro) {

    public static EquipoResumenDto de(Equipo e, String laboratorio, String responsableEquipo) {
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
                e.getResponsableEquipoId(),
                responsableEquipo,
                e.getFechaRegistro());
    }
}
