package inictel.edu.pe.prestamos.presentation.dto;

import inictel.edu.pe.prestamos.application.comando.CerrarUsoExternoComando;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Parte final del registro de uso externo: puntos 6 a 10 (RF-78). */
public record CerrarUsoExternoRequest(

        @NotNull(message = "Indique si el equipo se entregó en condiciones operativas.")
        Boolean entregadoOperativo,

        @NotNull(message = "Indique si el equipo se devolvió en condiciones operativas.")
        Boolean devueltoOperativo,

        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String incidente,

        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String accionCorrectiva,

        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String observaciones) {

    public CerrarUsoExternoComando aComando() {
        return new CerrarUsoExternoComando(entregadoOperativo, devueltoOperativo, incidente,
                accionCorrectiva, observaciones);
    }
}
