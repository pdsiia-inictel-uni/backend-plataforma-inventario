package inictel.edu.pe.organizacion.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de coordinaciones. Uso interno del adaptador. */
public interface CoordinacionJpaRepository extends JpaRepository<CoordinacionJpaEntity, Long> {

    List<CoordinacionJpaEntity> findAllByOrderByNombreAsc();

    List<CoordinacionJpaEntity> findByActivaTrueOrderByNombreAsc();

    List<CoordinacionJpaEntity> findByDireccionIdOrderByNombreAsc(Long direccionId);

    List<CoordinacionJpaEntity> findByDireccionIdAndActivaTrueOrderByNombreAsc(Long direccionId);

    boolean existsByDireccionIdAndNombreIgnoreCase(Long direccionId, String nombre);

    boolean existsByDireccionIdAndNombreIgnoreCaseAndIdNot(Long direccionId, String nombre, Long id);
}
