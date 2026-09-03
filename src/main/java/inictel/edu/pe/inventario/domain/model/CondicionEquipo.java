package inictel.edu.pe.inventario.domain.model;

/**
 * Condicion actual de un bien (RF-34).
 *
 * <p>Reemplaza al antiguo "estado" de la version 1.0. El valor inicial de todo
 * bien es {@link #OPERATIVO} (RN-13).</p>
 *
 * <p>La etiqueta cambia segun el contexto: en el inventario un bien esta
 * "Operativo", pero en la ventana de prestamos el mismo bien se ofrece como
 * "Disponible" (RF-58). Son dos palabras para el mismo hecho, cada una en el
 * lenguaje de quien la lee (RNF-29).</p>
 */
public enum CondicionEquipo {

    /** En servicio y sin novedad. Unica condicion desde la que se puede prestar. */
    OPERATIVO("Operativo", "Disponible"),

    /** Fuera de la coordinacion por un prestamo vigente. */
    PRESTADO("Prestado", "Prestado"),

    /** Retirado temporalmente del servicio por reparacion o revision. */
    MANTENIMIENTO("En mantenimiento", "En mantenimiento"),

    /** Baja logica. El bien se conserva en el historico (RNF-47). */
    BAJA("Dado de baja", "Dado de baja");

    private final String etiqueta;
    private final String etiquetaPrestamos;

    CondicionEquipo(String etiqueta, String etiquetaPrestamos) {
        this.etiqueta = etiqueta;
        this.etiquetaPrestamos = etiquetaPrestamos;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** RF-58: como se nombra la condicion en la ventana de prestamos. */
    public String getEtiquetaPrestamos() {
        return etiquetaPrestamos;
    }

    public boolean esPrestable() {
        return this == OPERATIVO;
    }
}
