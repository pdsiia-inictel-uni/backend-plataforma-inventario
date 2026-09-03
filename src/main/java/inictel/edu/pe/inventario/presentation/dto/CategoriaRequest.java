package inictel.edu.pe.inventario.presentation.dto;

import inictel.edu.pe.inventario.application.comando.GuardarCategoriaComando;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta y edicion de una categoria (RF-31). */
public record CategoriaRequest(

        @NotBlank(message = "Ingrese el nombre de la categoria.")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
        String nombre,

        @Size(max = 500, message = "La descripcion no puede superar los 500 caracteres.")
        String descripcion) {

    public GuardarCategoriaComando aComando(Long id) {
        return new GuardarCategoriaComando(id, nombre, descripcion, null);
    }
}
