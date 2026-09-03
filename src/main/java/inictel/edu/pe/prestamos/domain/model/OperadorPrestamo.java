package inictel.edu.pe.prestamos.domain.model;

/**
 * Usuario del sistema que registra la salida o recibe la devolucion (RF-24, RF-26).
 * Referencia por identidad al contexto {@code iam}, con el nombre como
 * instantanea para la trazabilidad.
 */
public record OperadorPrestamo(Long id, String nombreCompleto) {
}
