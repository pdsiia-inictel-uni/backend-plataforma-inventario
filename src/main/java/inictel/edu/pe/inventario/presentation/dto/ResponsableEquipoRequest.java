package inictel.edu.pe.inventario.presentation.dto;

/**
 * Quien pasa a tener el bien a su cargo (RF-83).
 *
 * @param responsableEquipoId Operador de la Coordinacion del bien, o
 *                            {@code null} para devolverlo a cargo del
 *                            Responsable, que es lo que el usuario ve como
 *                            "queda a mi nombre" (RN-37)
 */
public record ResponsableEquipoRequest(Long responsableEquipoId) {
}
