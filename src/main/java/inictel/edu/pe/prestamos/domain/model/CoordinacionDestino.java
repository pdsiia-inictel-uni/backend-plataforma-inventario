package inictel.edu.pe.prestamos.domain.model;

/**
 * Coordinacion a la que puede ir un equipo prestado (RF-59): cualquiera de la
 * institucion, de cualquier Direccion. Solo interesan su nombre y el de su
 * Direccion.
 */
public record CoordinacionDestino(Long id, String nombre, Long direccionId, String direccionNombre) {
}
