package inictel.edu.pe.inventario.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de categorias. Uso interno del adaptador. */
public interface CategoriaJpaRepository extends JpaRepository<CategoriaJpaEntity, Long> {

    List<CategoriaJpaEntity> findAllByOrderByNombreAsc();

    List<CategoriaJpaEntity> findByActivaTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
