package inictel.edu.pe.iam.application.comando;

import inictel.edu.pe.compartido.domain.seguridad.Rol;

/**
 * RF-28d: asignacion de un puesto a una persona ya registrada.
 *
 * @param rol            RESPONSABLE u OPERADOR dentro de una Coordinacion, o
 *                       ADMIN para el ambito institucional
 * @param coordinacionId Coordinacion del puesto; se ignora para ADMIN (RN-05)
 */
public record AsignarPuestoComando(Long usuarioId, Rol rol, Long coordinacionId) {
}
