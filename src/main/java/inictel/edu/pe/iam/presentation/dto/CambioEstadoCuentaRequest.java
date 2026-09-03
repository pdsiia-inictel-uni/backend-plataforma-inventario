package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import jakarta.validation.constraints.NotNull;

/**
 * Estado al que se lleva la cuenta de una persona (RF-22b).
 *
 * <p>Viaja el estado destino y no un "activo si o no": la diferencia entre
 * suspender y dar de baja es justamente la que un booleano no sabe expresar
 * —una es temporal y conserva el puesto; la otra es la salida de la institucion
 * y lo libera (RN-34)—, y era la que faltaba.</p>
 *
 * @param estado ACTIVA, SUSPENDIDA o BAJA. ACTIVA sirve tanto para terminar
 *               una suspension como para reincorporar a quien estaba de baja;
 *               el dominio sabe cual de las dos toca segun de donde venga
 */
public record CambioEstadoCuentaRequest(

        @NotNull(message = "Indique el estado de la cuenta.")
        EstadoCuenta estado) {
}
