package inictel.edu.pe.organizacion.domain.repository;

import inictel.edu.pe.organizacion.domain.model.Direccion;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Direccion. */
public interface DireccionRepositorio {

    Direccion guardar(Direccion direccion);

    Optional<Direccion> buscarPorId(Long id);

    /** Todas las direcciones, ordenadas por nombre. */
    List<Direccion> listar(boolean soloActivas);

    boolean existeNombre(String nombre, Long idActual);
}
