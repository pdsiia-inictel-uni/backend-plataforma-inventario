package inictel.edu.pe.prestamos.domain.model;

/**
 * Ciclo de vida de un prestamo (RF-24 .. RF-27).
 */
public enum EstadoPrestamo {

    ACTIVO("Activo"),
    DEVUELTO("Devuelto");

    private final String etiqueta;

    EstadoPrestamo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
