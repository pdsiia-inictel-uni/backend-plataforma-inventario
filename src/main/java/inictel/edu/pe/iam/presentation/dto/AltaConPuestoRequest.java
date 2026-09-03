package inictel.edu.pe.iam.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Alta de una persona con su puesto, en un solo acto (RF-16e).
 *
 * <p>Reune las dos peticiones que hasta la v3.8 el cliente encadenaba —el
 * registro previo (RF-16b) y la asignacion (RF-28d)— sin cambiar ninguna de
 * las dos: los mismos campos, las mismas validaciones y las mismas reglas.
 * Lo que cambia es que el servidor las resuelve dentro de una transaccion, de
 * modo que un fallo en la segunda no deja hecha la primera.</p>
 *
 * <p>Lo usa el Responsable cuando suma un Operador a su equipo, que es el
 * unico caso en que el puesto se sabe de antemano: solo puede crear
 * operadores, y solo en la Coordinacion que administra (RF-29). El
 * Administrador conserva los dos actos separados, porque en su pantalla el
 * puesto es justamente lo que esta por decidir.</p>
 */
public record AltaConPuestoRequest(

        @NotNull(message = "Faltan los datos de la persona.")
        @Valid
        UsuarioRequest datos,

        @NotNull(message = "Falta el puesto que va a ocupar.")
        @Valid
        AsignacionRequest puesto) {
}
