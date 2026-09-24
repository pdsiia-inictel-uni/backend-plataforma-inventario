package inictel.edu.pe.prestamos.application.comando;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Primera parte del registro de uso externo: puntos 1 a 5 del formato (RF-78).
 * El punto 2 —la identificacion del equipo— la pone el servidor a partir del
 * bien. La fecha y la hora de inicio del uso se escriben a mano.
 */
public record AbrirUsoExternoComando(
        String encargadoNombre,
        String encargadoCorreo,
        String encargadoCelular,
        String usuarioNombre,
        String usuarioCorreo,
        String usuarioTelefono,
        String proyecto,
        LocalDate fechaInicio,
        LocalTime horaInicio,
        LocalDate fechaFinPrevista,
        LocalTime horaFinPrevista,
        String actividad) {
}
