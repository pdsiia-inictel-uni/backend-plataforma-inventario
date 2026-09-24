package inictel.edu.pe.prestamos.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inictel.edu.pe.prestamos.application.comando.RegistrarPrestamoComando;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Registro de la salida de un bien (RF-59).
 *
 * <p>Primero se elige la coordinacion de destino y, dentro de ella, al
 * Responsable u Operador que se lleva el equipo. El cliente no envia nombres
 * ni DNI: el servidor los toma de la persona registrada.</p>
 */
public record PrestamoRequest(

        @NotNull(message = "Seleccione el bien a prestar.")
        Long equipoId,

        @NotNull(message = "Seleccione la coordinación de destino.")
        Long coordinacionDestinoId,

        @NotNull(message = "Seleccione a la persona que recibe el equipo.")
        Long personaUsuarioId,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaEstimadaDevolucion,

        @Size(max = 1000, message = "Las observaciones de salida no pueden superar los 1000 caracteres.")
        String observacionesSalida) {

    public RegistrarPrestamoComando aComando() {
        return new RegistrarPrestamoComando(equipoId, coordinacionDestinoId, personaUsuarioId,
                fechaEstimadaDevolucion, observacionesSalida);
    }
}
