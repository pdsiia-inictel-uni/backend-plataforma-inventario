package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.inventario.domain.model.Categoria;
import inictel.edu.pe.inventario.domain.repository.CategoriaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador de persistencia del agregado Categoria. */
@Repository
public class CategoriaRepositorioJpa implements CategoriaRepositorio {

    private final CategoriaJpaRepository jpa;

    public CategoriaRepositorioJpa(CategoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        CategoriaJpaEntity existente = categoria.getId() != null
                ? jpa.findById(categoria.getId()).orElse(null)
                : null;
        CategoriaJpaEntity guardada = jpa.save(InventarioMapper.aEntidad(categoria, existente));
        categoria.asignarId(guardada.getId());
        return InventarioMapper.aDominio(guardada);
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return id == null ? Optional.empty() : jpa.findById(id).map(InventarioMapper::aDominio);
    }

    @Override
    public List<Categoria> listar(boolean soloActivas) {
        List<CategoriaJpaEntity> entidades = soloActivas
                ? jpa.findByActivaTrueOrderByNombreAsc()
                : jpa.findAllByOrderByNombreAsc();
        return entidades.stream().map(InventarioMapper::aDominio).toList();
    }

    @Override
    public boolean existeNombre(String nombre, Long idExcluido) {
        return idExcluido == null
                ? jpa.existsByNombreIgnoreCase(nombre)
                : jpa.existsByNombreIgnoreCaseAndIdNot(nombre, idExcluido);
    }
}
