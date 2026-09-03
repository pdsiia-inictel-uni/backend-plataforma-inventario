package inictel.edu.pe.inventario.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inictel.edu.pe.inventario.application.comando.GuardarEquipoComando;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Alta y edicion de un bien (RF-34, RF-40).
 *
 * <p>No admite condicion, coordinacion, responsable ni fechas: son
 * atribuciones del servidor. Lo que el formulario no puede enviar, el cliente
 * no puede falsear (RF-35 .. RF-37, RN-21).</p>
 */
public record EquipoRequest(

        @NotBlank(message = "Ingrese el nombre del equipo.")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
        String nombre,

        @NotBlank(message = "Ingrese la marca del equipo.")
        @Size(max = 100, message = "La marca no puede superar los 100 caracteres.")
        String marca,

        @NotBlank(message = "Ingrese el modelo del equipo.")
        @Size(max = 100, message = "El modelo no puede superar los 100 caracteres.")
        String modelo,

        @NotBlank(message = "Ingrese el numero de serie. Si el equipo no tiene, escriba S/N.")
        @Size(max = 100, message = "El numero de serie no puede superar los 100 caracteres.")
        String numeroSerie,

        @NotBlank(message = "Ingrese el codigo de inventario.")
        @Size(max = 60, message = "El codigo de inventario no puede superar los 60 caracteres.")
        String codigoInventario,

        @NotBlank(message = "Ingrese el codigo patrimonial.")
        @Size(max = 60, message = "El codigo patrimonial no puede superar los 60 caracteres.")
        String codigoPatrimonial,

        @NotNull(message = "Seleccione la categoria del bien.")
        Long categoriaId,

        /** Opcional: ubicacion fisica dentro de la coordinacion (RN-12). */
        Long laboratorioId,

        @NotNull(message = "Ingrese la fecha de adquisicion.")
        @PastOrPresent(message = "La fecha de adquisicion no puede ser futura.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaAdquisicion,

        @NotNull(message = "Ingrese el costo del equipo.")
        @DecimalMin(value = "0.0", message = "El costo no puede ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "El costo admite como maximo dos decimales.")
        BigDecimal costo,

        @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres.")
        String observaciones) {

    public GuardarEquipoComando aComando(Long id) {
        return new GuardarEquipoComando(id, nombre, marca, modelo, numeroSerie,
                codigoInventario, codigoPatrimonial, categoriaId, laboratorioId,
                fechaAdquisicion, costo, observaciones);
    }
}
