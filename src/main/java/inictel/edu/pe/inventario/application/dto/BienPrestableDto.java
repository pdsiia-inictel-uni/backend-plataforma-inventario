package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.inventario.domain.model.Equipo;

/**
 * Vista minima de un bien para el contexto de prestamos: lo justo para
 * identificarlo y saber si puede salir.
 *
 * <p>La etiqueta usa el vocabulario de prestamos, donde un bien OPERATIVO se
 * ofrece como "Disponible" (RF-58, RNF-29).</p>
 */
public record BienPrestableDto(
        Long id,
        Long coordinacionId,
        String nombre,
        String marca,
        String modelo,
        String codigoInventario,
        String numeroSerie,
        String condicionEtiqueta,
        boolean activo,
        boolean disponible) {

    public static BienPrestableDto de(Equipo e) {
        return new BienPrestableDto(
                e.getId(),
                e.getCoordinacionId(),
                e.getNombre(),
                e.getMarca(),
                e.getModelo(),
                e.getCodigoInventario().valor(),
                e.getNumeroSerie().valor(),
                e.getCondicion().getEtiquetaPrestamos(),
                e.isActivo(),
                e.estaDisponible());
    }
}
