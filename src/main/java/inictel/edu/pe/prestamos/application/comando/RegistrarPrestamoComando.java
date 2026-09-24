package inictel.edu.pe.prestamos.application.comando;

import java.time.LocalDate;

/**
 * Registro de la salida de un bien (RF-59). El equipo se entrega a un
 * Responsable u Operador registrado de la coordinacion de destino; su nombre,
 * su DNI y el nombre de la coordinacion los resuelve el servidor. La fecha y
 * hora del prestamo tambien.
 */
public record RegistrarPrestamoComando(
        Long equipoId,
        Long coordinacionDestinoId,
        Long personaUsuarioId,
        LocalDate fechaEstimadaDevolucion,
        String observacionesSalida) {
}
