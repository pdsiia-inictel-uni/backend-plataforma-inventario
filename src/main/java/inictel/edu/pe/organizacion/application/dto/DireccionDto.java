package inictel.edu.pe.organizacion.application.dto;

import inictel.edu.pe.organizacion.domain.model.Direccion;

public record DireccionDto(
        Long id,
        String nombre,
        String sigla) {

    public static DireccionDto de(Direccion direccion) {
        return new DireccionDto(direccion.getId(), direccion.getNombre(), direccion.getSigla());
    }
}
