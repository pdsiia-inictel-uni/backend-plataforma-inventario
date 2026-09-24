package inictel.edu.pe.prestamos.domain.model;

/** Situacion de un uso externo: abierto mientras el equipo esta fuera, cerrado al volver. */
public enum EstadoUso {

    ABIERTO("En uso"),
    CERRADO("Cerrado");

    private final String etiqueta;

    EstadoUso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
