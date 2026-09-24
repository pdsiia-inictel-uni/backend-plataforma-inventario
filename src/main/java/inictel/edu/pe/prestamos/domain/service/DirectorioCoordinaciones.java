package inictel.edu.pe.prestamos.domain.service;

import inictel.edu.pe.prestamos.domain.model.CoordinacionDestino;

import java.util.List;
import java.util.Optional;

/**
 * Puerto hacia {@code organizacion}: el nombre de la coordinacion de destino
 * de un prestamo, que queda escrito en el propio prestamo (RNF-39).
 */
public interface DirectorioCoordinaciones {

    Optional<String> nombreDe(Long coordinacionId);

    /** RF-59: todas las coordinaciones de todas las Direcciones. */
    List<CoordinacionDestino> todas();
}
