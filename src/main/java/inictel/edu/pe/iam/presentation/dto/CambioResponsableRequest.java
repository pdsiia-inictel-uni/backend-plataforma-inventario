package inictel.edu.pe.iam.presentation.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Persona que toma el puesto de Responsable de una Coordinacion (RF-26b).
 *
 * <p>La Coordinacion viaja en la ruta y no aqui: la operacion se hace sobre
 * ella —es su puesto el que cambia de manos— y lo unico que hay que decidir es
 * quien lo ocupa. Al saliente no hay que nombrarlo: es quien lo ocupa ahora, y
 * eso lo sabe el servidor (RN-35).</p>
 */
public record CambioResponsableRequest(

        @NotNull(message = "Elija a la persona que se hara cargo de la coordinacion.")
        Long usuarioId) {
}
