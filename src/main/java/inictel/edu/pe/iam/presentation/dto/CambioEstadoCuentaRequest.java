package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import jakarta.validation.constraints.NotNull;

/**
 * Estado al que se lleva la cuenta de una persona (RF-22b).
 *
 * <p>Viaja el estado destino y no un "activo si o no" porque lo que se decide
 * tiene nombre propio: la <b>baja</b> es la salida de la institucion y libera
 * el puesto (RN-34); la vuelta a <b>ACTIVA</b> es la reincorporacion de quien
 * regresa, que recupera su cuenta pero no el puesto que dejo.</p>
 *
 * @param estado BAJA para dar de baja; ACTIVA para reincorporar a quien lo
 *               estaba. No hay un tercer valor: la suspension temporal se
 *               retiro en la v3.12
 */
public record CambioEstadoCuentaRequest(

        @NotNull(message = "Indique el estado de la cuenta.")
        EstadoCuenta estado) {
}
