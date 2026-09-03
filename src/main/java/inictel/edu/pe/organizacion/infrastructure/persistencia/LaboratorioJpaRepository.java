package inictel.edu.pe.organizacion.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Repositorio Spring Data de laboratorios. Uso interno del adaptador. */
public interface LaboratorioJpaRepository extends JpaRepository<LaboratorioJpaEntity, Long> {

    List<LaboratorioJpaEntity> findByCoordinacionIdOrderByNombreAsc(Long coordinacionId);

    List<LaboratorioJpaEntity> findByCoordinacionIdAndActivoTrueOrderByNombreAsc(Long coordinacionId);

    boolean existsByCoordinacionIdAndNombreIgnoreCase(Long coordinacionId, String nombre);

    boolean existsByCoordinacionIdAndNombreIgnoreCaseAndIdNot(Long coordinacionId, String nombre, Long id);

    long countByCoordinacionIdAndActivoTrue(Long coordinacionId);
}
