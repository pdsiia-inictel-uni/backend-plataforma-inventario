package inictel.edu.pe.organizacion.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta de una coordinacion junto a su primer laboratorio (RF-11, RN-26).
 *
 * <p>La v3 separa el alta de la edicion porque solo el alta exige el primer
 * laboratorio: una coordinacion recien creada sin ninguno no podria registrar
 * bienes, y quedaria inservible hasta que alguien lo recordara.</p>
 */
public record NuevaCoordinacionRequest(

        @NotNull(message = "Seleccione la direccion a la que pertenece.")
        Long direccionId,

        @NotBlank(message = "Ingrese el nombre de la coordinacion.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripcion no puede superar los 500 caracteres.")
        String descripcion,

        @NotBlank(message = "Ingrese el nombre del primer laboratorio de la coordinacion.")
        @Size(max = 150, message = "El nombre del laboratorio no puede superar los 150 caracteres.")
        String laboratorioNombre,

        @Size(max = 200, message = "La ubicacion no puede superar los 200 caracteres.")
        String laboratorioUbicacion) {
}
