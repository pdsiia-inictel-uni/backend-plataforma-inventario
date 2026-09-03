package inictel.edu.pe.reportes.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inictel.edu.pe.reportes.application.comando.GenerarFormatoUsoComando;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Lo que se escribe a mano en el formato de registro de uso (RF-78).
 *
 * <p>Ningun campo es obligatorio, y es deliberado: el formato se imprime para
 * firmarse, y la mitad de sus casillas se rellenan con el boligrafo delante
 * del equipo —la hora de fin, la conformidad de la devolucion, el incidente
 * que todavia no ha ocurrido—. Exigirlas en pantalla obligaria a inventarlas.
 * Lo unico que se comprueba es la medida de cada campo, para que el documento
 * siga cabiendo en su recuadro.</p>
 *
 * <p>Los datos del equipo no viajan aqui: los pone el servidor a partir del
 * bien, que es quien los tiene registrados (RF-78).</p>
 */
public record FormatoUsoRequest(

        @Size(max = 150, message = "El nombre del investigador encargado no puede superar los 150 caracteres.")
        String investigadorEncargado,

        @Email(message = "El correo del encargado no tiene un formato valido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        String correoEncargado,

        @Size(max = 30, message = "El celular no puede superar los 30 caracteres.")
        String celularEncargado,

        @Size(max = 150, message = "El nombre del investigador no puede superar los 150 caracteres.")
        String nombreInvestigador,

        @Email(message = "El correo institucional no tiene un formato valido.")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres.")
        String correoInvestigador,

        @Size(max = 30, message = "El telefono no puede superar los 30 caracteres.")
        String telefonoInvestigador,

        @Size(max = 1000, message = "El proyecto y la actividad no pueden superar los 1000 caracteres.")
        String proyecto,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaFinUso,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime horaFinUso,

        @Size(max = 1000, message = "La actividad realizada no puede superar los 1000 caracteres.")
        String actividadRealizada,

        Boolean entregadoOperativo,

        Boolean devueltoOperativo,

        @Size(max = 1000, message = "La descripcion del incidente no puede superar los 1000 caracteres.")
        String incidente,

        @Size(max = 1000, message = "La accion correctiva no puede superar los 1000 caracteres.")
        String accionCorrectiva,

        @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres.")
        String observaciones) {

    public GenerarFormatoUsoComando aComando() {
        return new GenerarFormatoUsoComando(
                investigadorEncargado, correoEncargado, celularEncargado,
                nombreInvestigador, correoInvestigador, telefonoInvestigador,
                proyecto,
                fechaFinUso, horaFinUso, actividadRealizada,
                entregadoOperativo, devueltoOperativo,
                incidente, accionCorrectiva, observaciones);
    }
}
