package inictel.edu.pe.organizacion.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de direcciones. Uso interno del adaptador. */
public interface DireccionJpaRepository extends JpaRepository<DireccionJpaEntity, Long> {

    List<DireccionJpaEntity> findAllByOrderByNombreAsc();

    List<DireccionJpaEntity> findByActivaTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
