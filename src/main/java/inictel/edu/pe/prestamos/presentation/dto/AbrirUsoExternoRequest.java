package inictel.edu.pe.prestamos.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inictel.edu.pe.prestamos.application.comando.AbrirUsoExternoComando;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/** Primera parte del registro de uso externo: puntos 1 a 5 (RF-78). */
public record AbrirUsoExternoRequest(

        @NotBlank(message = "Indique el investigador encargado.")
        @Size(max = 150, message = "Máximo 150 caracteres.")
        String encargadoNombre,

        @Email(message = "Escriba un correo válido.")
        @Size(max = 150, message = "Máximo 150 caracteres.")
        String encargadoCorreo,

        @Size(max = 30, message = "Máximo 30 caracteres.")
        String encargadoCelular,

        @NotBlank(message = "Indique el nombre de quien usará el equipo.")
        @Size(max = 150, message = "Máximo 150 caracteres.")
        String usuarioNombre,

        @Email(message = "Escriba un correo válido.")
        @Size(max = 150, message = "Máximo 150 caracteres.")
        String usuarioCorreo,

        @Size(max = 30, message = "Máximo 30 caracteres.")
        String usuarioTelefono,

        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String proyecto,

        @NotNull(message = "Indique la fecha de inicio del uso.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaInicio,

        @NotNull(message = "Indique la hora de inicio del uso.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaFinPrevista,

        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFinPrevista,

        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String actividad) {

    public AbrirUsoExternoComando aComando() {
        return new AbrirUsoExternoComando(encargadoNombre, encargadoCorreo, encargadoCelular,
                usuarioNombre, usuarioCorreo, usuarioTelefono, proyecto, fechaInicio, horaInicio, fechaFinPrevista,
                horaFinPrevista, actividad);
    }
}
