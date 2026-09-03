package inictel.edu.pe.organizacion.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta y edicion de un laboratorio (RF-12). */
public record LaboratorioRequest(

        @NotNull(message = "Seleccione la coordinacion a la que pertenece.")
        Long coordinacionId,

        @NotBlank(message = "Ingrese el nombre del laboratorio.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String nombre,

        @Size(max = 200, message = "La ubicacion no puede superar los 200 caracteres.")
        String ubicacion) {
}
