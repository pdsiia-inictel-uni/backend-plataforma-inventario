package inictel.edu.pe.organizacion.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta y edicion de una coordinacion (RF-11). */
public record CoordinacionRequest(

        @NotNull(message = "Seleccione la direccion a la que pertenece.")
        Long direccionId,

        @NotBlank(message = "Ingrese el nombre de la coordinacion.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripcion no puede superar los 500 caracteres.")
        String descripcion) {
}
