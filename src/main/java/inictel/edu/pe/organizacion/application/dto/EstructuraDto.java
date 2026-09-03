package inictel.edu.pe.organizacion.application.dto;

import inictel.edu.pe.organizacion.domain.model.Institucion;

import java.util.List;

/**
 * Arbol completo de la institucion, tal como lo consume la pagina principal del
 * Administrador (ERS v2.0, seccion 8.2).
 *
 * <p>Se entrega en una sola peticion, ya resuelto, para que la vista no tenga
 * que encadenar llamadas por cada nivel.</p>
 */
public record EstructuraDto(String institucion, List<RamaDireccion> direcciones, long coordinacionesSinResponsable) {

    public record RamaDireccion(DireccionDto direccion, List<CoordinacionDto> coordinaciones) {
    }

    public static EstructuraDto de(List<RamaDireccion> direcciones) {
        long sinResponsable = direcciones.stream()
                .flatMap(rama -> rama.coordinaciones().stream())
                .filter(CoordinacionDto::sinResponsable)
                .count();
        return new EstructuraDto(Institucion.NOMBRE, direcciones, sinResponsable);
    }
}
