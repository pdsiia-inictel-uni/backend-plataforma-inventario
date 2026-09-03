package inictel.edu.pe.prestamos.domain.model;

/**
 * Instantanea del bien que sale en prestamo.
 *
 * <p>El bien pertenece al contexto {@code inventario}; aqui se referencia por
 * identidad y se conservan los datos minimos para identificarlo en los listados
 * y en el historial (RF-66).</p>
 *
 * <p>Incluye la Coordinacion porque el prestamo la hereda del bien: es lo que
 * permite aislar los prestamos de cada coordinacion sin volver a preguntarle a
 * inventario en cada consulta (RN-23).</p>
 */
public record BienPrestado(
        Long id,
        Long coordinacionId,
        String nombre,
        String codigoInventario,
        String numeroSerie) {
}
