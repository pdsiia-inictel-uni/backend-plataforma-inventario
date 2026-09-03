package inictel.edu.pe.prestamos.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inictel.edu.pe.prestamos.application.comando.RegistrarPrestamoComando;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Registro de la salida de un bien (RF-24). */
public record PrestamoRequest(

        @NotNull(message = "Seleccione el bien a prestar.")
        Long equipoId,

        @NotBlank(message = "Ingrese el nombre completo de la persona que lleva el bien.")
        @Size(max = 200, message = "El nombre no puede superar los 200 caracteres.")
        String nombrePersona,

        @NotBlank(message = "Ingrese el DNI de la persona.")
        @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 digitos numericos.")
        String dniPersona,

        @Size(max = 200, message = "El destino no puede superar los 200 caracteres.")
        String destino,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaEstimadaDevolucion,

        @Size(max = 1000, message = "Las observaciones de salida no pueden superar los 1000 caracteres.")
        String observacionesSalida) {

    public RegistrarPrestamoComando aComando() {
        return new RegistrarPrestamoComando(equipoId, nombrePersona, dniPersona, destino,
                fechaEstimadaDevolucion, observacionesSalida);
    }
}
