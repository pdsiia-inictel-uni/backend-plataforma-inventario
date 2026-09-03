package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.inventario.domain.model.Categoria;

import java.time.LocalDateTime;

/** Categoria del catalogo institucional de bienes (RF-31). */
public record CategoriaDto(
        Long id,
        String nombre,
        String descripcion,
        boolean activa,
        LocalDateTime fechaCreacion) {

    public static CategoriaDto de(Categoria c) {
        return new CategoriaDto(
                c.getId(),
                c.getNombre(),
                c.getDescripcion(),
                c.isActiva(),
                c.getFechaCreacion());
    }
}
