package inictel.edu.pe.inventario.domain.repository;

import inictel.edu.pe.inventario.domain.model.Categoria;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Categoria. */
public interface CategoriaRepositorio {

    Categoria guardar(Categoria categoria);

    Optional<Categoria> buscarPorId(Long id);

    List<Categoria> listar(boolean soloActivas);

    boolean existeNombre(String nombre, Long idExcluido);
}
