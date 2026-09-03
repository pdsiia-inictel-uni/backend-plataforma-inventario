package inictel.edu.pe.inventario.domain.model;

/**
 * Referencia a la categoria desde el agregado Equipo.
 *
 * <p>Los agregados se referencian por identidad; se conserva ademas el nombre
 * como instantanea para no tener que cargar la categoria completa al listar.</p>
 */
public record ReferenciaCategoria(Long id, String nombre) {
}
