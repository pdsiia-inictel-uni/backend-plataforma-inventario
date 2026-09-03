package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.Equipo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Ficha completa de un bien (RF-34). */
public record EquipoDto(
        Long id,
        Long coordinacionId,
        String coordinacion,
        Long laboratorioId,
        String laboratorio,
        String nombre,
        String marca,
        String modelo,
        String numeroSerie,
        String codigoInventario,
        String codigoPatrimonial,
        Long categoriaId,
        String categoria,
        CondicionEquipo condicion,
        String condicionEtiqueta,
        LocalDate fechaAdquisicion,
        BigDecimal costo,
        String observaciones,
        String fotoUrl,
        boolean revisionPendiente,
        String motivoBaja,
        LocalDateTime fechaBaja,
        Long responsableId,
        String responsable,
        /**
         * RF-83: Operador a cargo del bien, o {@code null} si lo lleva el
         * Responsable de la Coordinacion.
         */
        Long responsableEquipoId,
        /**
         * Nombre de quien responde por el bien hoy: el Operador a su cargo o,
         * si no lo tiene, el Responsable vigente. Siempre hay uno, de modo que
         * la ficha nunca dice "sin responsable".
         */
        String responsableEquipo,
        String registradoPor,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaActualizacion,
        boolean activo) {

    public static EquipoDto de(Equipo e, String coordinacion, String laboratorio,
                               String responsable, String responsableEquipo, String registradoPor) {
        return new EquipoDto(
                e.getId(),
                e.getCoordinacionId(),
                coordinacion,
                e.getLaboratorioId(),
                laboratorio,
                e.getNombre(),
                e.getMarca(),
                e.getModelo(),
                e.getNumeroSerie().valor(),
                e.getCodigoInventario().valor(),
                e.getCodigoPatrimonial().valor(),
                e.getCategoria() == null ? null : e.getCategoria().id(),
                e.getCategoria() == null ? null : e.getCategoria().nombre(),
                e.getCondicion(),
                e.getCondicion().getEtiqueta(),
                e.getFechaAdquisicion(),
                e.getCosto(),
                e.getObservaciones(),
                e.getFotoUrl(),
                e.isRevisionPendiente(),
                e.getMotivoBaja(),
                e.getFechaBaja(),
                e.getResponsableId(),
                responsable,
                e.getResponsableEquipoId(),
                responsableEquipo,
                registradoPor,
                e.getFechaRegistro(),
                e.getFechaActualizacion(),
                e.isActivo());
    }
}
