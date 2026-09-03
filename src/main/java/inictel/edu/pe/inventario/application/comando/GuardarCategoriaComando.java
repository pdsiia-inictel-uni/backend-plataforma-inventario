package inictel.edu.pe.inventario.application.comando;

/** Alta o edicion de una categoria del catalogo institucional (RF-31). */
public record GuardarCategoriaComando(
        Long id,
        String nombre,
        String descripcion,
        Boolean activa) {
}
