package inictel.edu.pe.prestamos.application.comando;

import java.time.LocalDate;

/**
 * Registro de la salida de un bien (RF-24). La fecha y hora del prestamo las
 * genera el servidor.
 */
public record RegistrarPrestamoComando(
        Long equipoId,
        String nombrePersona,
        String dniPersona,
        String destino,
        LocalDate fechaEstimadaDevolucion,
        String observacionesSalida) {
}
