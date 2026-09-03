package inictel.edu.pe.prestamos.presentation.dto;

import inictel.edu.pe.prestamos.application.comando.RegistrarDevolucionComando;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Devolucion con checklist de conformidad (RF-61, RF-65).
 *
 * <p>El formulario ofrece dos opciones claras: "conforme, completo y en buen
 * estado" o "con observaciones", que obliga a detallar el motivo.</p>
 */
public record DevolucionRequest(

        @NotNull(message = "Debe confirmar la conformidad de la devolucion.")
        Boolean conforme,

        Boolean reportaDano,

        @Size(max = 1000, message = "Las observaciones de retorno no pueden superar los 1000 caracteres.")
        String observacionesRetorno) {

    public RegistrarDevolucionComando aComando(Long prestamoId) {
        return new RegistrarDevolucionComando(prestamoId, conforme, reportaDano, observacionesRetorno);
    }
}
