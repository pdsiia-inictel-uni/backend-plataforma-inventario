package inictel.edu.pe.inventario.domain.model;

/**
 * Clase de hecho registrado en la linea de tiempo de un bien (RF-53).
 *
 * <p>Cada valor corresponde a un acontecimiento del ciclo de vida patrimonial;
 * su etiqueta es la que lee el usuario en la ficha del bien, redactada en
 * lenguaje natural (RF-56).</p>
 */
public enum TipoMovimiento {

    ALTA("Registrado en el inventario"),
    PRESTAMO("Prestado"),
    DEVOLUCION("Devuelto"),
    MANTENIMIENTO("Enviado a mantenimiento"),
    OPERATIVO("Devuelto a condición operativa"),
    BAJA("Dado de baja"),
    /** Historico: la reincorporacion de bienes dados de baja ya no existe; se conserva para leer movimientos antiguos. */
    REINCORPORACION("Reincorporado al inventario"),
    EDICION("Datos modificados"),
    REUBICACION("Reubicado de laboratorio"),
    RESPONSABLE("Cambio de responsable del equipo");

    private final String etiqueta;

    TipoMovimiento(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
