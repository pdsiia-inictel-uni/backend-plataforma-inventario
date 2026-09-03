package inictel.edu.pe.organizacion.application.dto;

import inictel.edu.pe.organizacion.domain.model.Direccion;

import java.time.LocalDateTime;

public record DireccionDto(
        Long id,
        String nombre,
        String sigla,
        String descripcion,
        boolean activa,
        LocalDateTime fechaCreacion) {

    public static DireccionDto de(Direccion direccion) {
        return new DireccionDto(
                direccion.getId(),
                direccion.getNombre(),
                direccion.getSigla(),
                direccion.getDescripcion(),
                direccion.isActiva(),
                direccion.getFechaCreacion());
    }
}
