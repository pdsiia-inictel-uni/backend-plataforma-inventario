package inictel.edu.pe.reportes.application.comando;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Lo que una persona escribe a mano en el formato de registro de uso (RF-78).
 *
 * <p>Es la mitad del documento que el sistema no sabe: quien lo va a usar, en
 * que proyecto, hasta cuando y con que resultado. La otra mitad —el equipo,
 * su codigo, su valor y la condicion en que esta— la pone el servidor a
 * partir del bien, porque ya la tiene registrada y volver a pedirla es pedir
 * que se copie mal.</p>
 *
 * <p><b>Nada de esto se guarda.</b> El comando viaja, se imprime en un PDF y
 * se descarta: el formato es un papel de laboratorio, no un prestamo, y el
 * prestamo tiene su propio registro (RF-59).</p>
 *
 * @param investigadorEncargado responsable del equipamiento, punto 1
 * @param correoEncargado       correo del encargado, punto 1
 * @param celularEncargado      celular del encargado, punto 1
 * @param nombreInvestigador    quien va a usar el equipo, punto 3
 * @param correoInvestigador    su correo institucional, punto 3
 * @param telefonoInvestigador  su telefono de contacto, punto 3
 * @param proyecto              proyecto asociado y actividad, punto 4
 * @param fechaFinUso           fecha prevista de fin de uso, punto 5
 * @param horaFinUso            hora prevista de fin de uso, punto 5
 * @param actividadRealizada    actividad para la que se usa, punto 5
 * @param entregadoOperativo    punto 6; {@code null} deja las dos casillas vacias
 * @param devueltoOperativo     punto 7; {@code null} deja las dos casillas vacias
 * @param incidente             descripcion del incidente o falla, punto 8
 * @param accionCorrectiva      accion correctiva realizada, punto 8
 * @param observaciones         observaciones generales, punto 10
 */
public record GenerarFormatoUsoComando(
        String investigadorEncargado,
        String correoEncargado,
        String celularEncargado,
        String nombreInvestigador,
        String correoInvestigador,
        String telefonoInvestigador,
        String proyecto,
        LocalDate fechaFinUso,
        LocalTime horaFinUso,
        String actividadRealizada,
        Boolean entregadoOperativo,
        Boolean devueltoOperativo,
        String incidente,
        String accionCorrectiva,
        String observaciones) {
}
