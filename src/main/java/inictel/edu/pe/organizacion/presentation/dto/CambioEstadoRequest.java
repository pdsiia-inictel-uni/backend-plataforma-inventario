package inictel.edu.pe.organizacion.presentation.dto;

import jakarta.validation.constraints.NotNull;

/** Activacion o desactivacion de un elemento de la estructura. */
public record CambioEstadoRequest(

        @NotNull(message = "Indique si el elemento queda activo o inactivo.")
        Boolean activo) {
}
