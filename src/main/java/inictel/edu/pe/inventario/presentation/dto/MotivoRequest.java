package inictel.edu.pe.inventario.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Motivo de una accion que cambia la condicion de un bien (RF-41 .. RF-43).
 *
 * <p>El motivo es obligatorio porque queda en el historial del bien y es lo que
 * permite responder mas tarde "por que se dio de baja este equipo" (RF-53).</p>
 */
public record MotivoRequest(

        @NotBlank(message = "Indique el motivo.")
        @Size(max = 500, message = "El motivo no puede superar los 500 caracteres.")
        String motivo) {
}
