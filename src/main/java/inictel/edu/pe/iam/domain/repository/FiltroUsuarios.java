package inictel.edu.pe.iam.domain.repository;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;

/**
 * Criterios de busqueda de usuarios. Los campos nulos no filtran.
 *
 * @param texto          busqueda libre por nombres, apellidos, usuario, correo o DNI
 * @param rol            rol exacto; sostiene el filtro por rol de la pantalla
 *                       unica de personas (RF-28)
 * @param coordinacionId asignacion; el servicio lo fuerza al valor del usuario
 *                       en curso cuando quien consulta no es Administrador (RN-23)
 * @param sinAsignar     TRUE devuelve solo a las personas registradas que aun no
 *                       tienen puesto; FALSE, solo a las que ya lo tienen (RF-28e)
 * @param estado         situacion de la cuenta: activa o de baja (RF-22b)
 *                       (RF-22b). Desde la v3.7 son tres y no dos, porque
 *                       irse de vacaciones y dejar la institucion no son lo
 *                       mismo y la lista tiene que poder distinguirlos
 */
public record FiltroUsuarios(String texto,
                             Rol rol,
                             Long coordinacionId,
                             Boolean sinAsignar,
                             EstadoCuenta estado) {

    public static FiltroUsuarios vacio() {
        return new FiltroUsuarios(null, null, null, null, null);
    }

    /** Devuelve una copia con la Coordinacion fijada, para aplicar el aislamiento. */
    public FiltroUsuarios acotadoA(Long coordinacion) {
        return new FiltroUsuarios(texto, rol, coordinacion, sinAsignar, estado);
    }
}
