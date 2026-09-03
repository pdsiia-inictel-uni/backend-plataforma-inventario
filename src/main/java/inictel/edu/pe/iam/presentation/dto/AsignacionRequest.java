package inictel.edu.pe.iam.presentation.dto;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.application.comando.AsignarPuestoComando;
import jakarta.validation.constraints.NotNull;

/**
 * Puesto que se le da a una persona ya registrada (RF-28d).
 *
 * <p>Es el segundo de los dos actos en que se reparte el alta: el primero dijo
 * quien es la persona; este dice que hace y donde. Aqui es tambien donde nace
 * su contrasena, porque es cuando la cuenta empieza a poder usarse.</p>
 *
 * @param rol            RESPONSABLE u OPERADOR dentro de una Coordinacion, o
 *                       ADMIN para el ambito institucional
 * @param coordinacionId Coordinacion del puesto. Obligatoria para los dos roles
 *                       operativos; el ADMIN no pertenece a ninguna (RN-05)
 */
public record AsignacionRequest(

        @NotNull(message = "Seleccione el rol que va a desempenar.")
        Rol rol,

        Long coordinacionId) {

    public AsignarPuestoComando aComando(Long usuarioId) {
        return new AsignarPuestoComando(usuarioId, rol, rol == Rol.ADMIN ? null : coordinacionId);
    }
}
