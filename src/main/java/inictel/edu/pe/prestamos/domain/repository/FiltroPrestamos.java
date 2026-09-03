package inictel.edu.pe.prestamos.domain.repository;

import inictel.edu.pe.prestamos.domain.model.EstadoPrestamo;

import java.time.LocalDate;

/**
 * Criterios de busqueda de prestamos (RF-66, RF-67). Los campos nulos no
 * filtran, salvo {@code coordinacionId}, que impone el servicio (RN-23).
 */
public record FiltroPrestamos(
        Long coordinacionId,
        String texto,
        EstadoPrestamo estado,
        Long equipoId,
        String dni,
        LocalDate desde,
        LocalDate hasta,
        Boolean soloVencidos) {

    public static FiltroPrestamos vacio() {
        return new FiltroPrestamos(null, null, null, null, null, null, null, null);
    }

    public FiltroPrestamos enCoordinacion(Long coordinacion) {
        return new FiltroPrestamos(coordinacion, texto, estado, equipoId, dni, desde, hasta, soloVencidos);
    }
}
