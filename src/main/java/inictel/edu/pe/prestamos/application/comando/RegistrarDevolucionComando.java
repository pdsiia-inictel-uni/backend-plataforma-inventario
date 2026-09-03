package inictel.edu.pe.prestamos.application.comando;

/**
 * Devolucion de un bien con la validacion de conformidad (RF-61).
 *
 * @param reportaDano se recibio danado o incompleto; marca el bien para
 *                    revision del Responsable, no lo envia a mantenimiento
 *                    por si solo (RN-19)
 */
public record RegistrarDevolucionComando(
        Long prestamoId,
        Boolean conforme,
        Boolean reportaDano,
        String observacionesRetorno) {
}
