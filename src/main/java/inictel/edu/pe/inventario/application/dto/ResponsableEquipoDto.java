package inictel.edu.pe.inventario.application.dto;

/**
 * Una opcion del listado por responsable de equipo (RF-84).
 *
 * <p>Es el reparto del inventario dicho en una linea: quien lleva equipos en
 * esta Coordinacion y cuantos. Sirve para poblar el desplegable del listado y
 * para que quien lo abre vea el reparto de un vistazo, sin tener que elegir un
 * nombre a ciegas y descubrir despues que no lleva ninguno (RNF-23).</p>
 *
 * @param usuarioId                  {@code null} en la fila del Responsable de
 *                                   la Coordinacion, porque sus bienes son
 *                                   justamente los que no tienen asignacion
 *                                   (RN-37)
 * @param nombreCompleto             como se le nombra en el desplegable
 * @param esResponsableCoordinacion  true en esa fila, para que la pantalla la
 *                                   presente primero y con su titulo
 * @param cantidad                   bienes en servicio a su nombre
 */
public record ResponsableEquipoDto(
        Long usuarioId,
        String nombreCompleto,
        boolean esResponsableCoordinacion,
        long cantidad) {
}
