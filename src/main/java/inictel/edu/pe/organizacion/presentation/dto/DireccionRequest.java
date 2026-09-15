package inictel.edu.pe.organizacion.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Correccion de los datos de una direccion (RF-10). */
public record DireccionRequest(

        @NotBlank(message = "Ingrese el nombre de la direccion.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String nombre,

        @Size(max = 20, message = "La sigla no puede superar los 20 caracteres.")
        String sigla) {
}
