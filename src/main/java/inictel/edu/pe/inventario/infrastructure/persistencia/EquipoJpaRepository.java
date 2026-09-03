package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Repositorio Spring Data de bienes. Uso interno del adaptador. */
public interface EquipoJpaRepository
        extends JpaRepository<EquipoJpaEntity, Long>, JpaSpecificationExecutor<EquipoJpaEntity> {

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    boolean existsByNumeroSerieIgnoreCaseAndIdNot(String numeroSerie, Long id);

    boolean existsByCodigoInventarioIgnoreCase(String codigo);

    boolean existsByCodigoInventarioIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByCodigoPatrimonialIgnoreCase(String codigo);

    boolean existsByCodigoPatrimonialIgnoreCaseAndIdNot(String codigo, Long id);

    long countByCategoriaIdAndActivoTrue(Long categoriaId);

    /** RN-38: bienes en servicio a nombre de una persona. */
    long countByResponsableEquipoIdAndActivoTrue(Long responsableEquipoId);

    List<EquipoJpaEntity> findByResponsableEquipoIdAndActivoTrue(Long responsableEquipoId);

    long countByCoordinacionIdAndActivoTrue(Long coordinacionId);

    long countByLaboratorioId(Long laboratorioId);

    long countByCoordinacionIdAndRevisionPendienteTrue(Long coordinacionId);

    long countByRevisionPendienteTrue();

    /**
     * Conteo por condicion en una sola consulta agrupada.
     *
     * <p>Con {@code coordinacionId} nulo abarca toda la institucion. Agrupar en
     * la base evita cuatro viajes por cada tarjeta del panel (RNF-12).</p>
     */
    @Query("""
            SELECT e.condicion, COUNT(e)
            FROM EquipoJpaEntity e
            WHERE (:coordinacionId IS NULL OR e.coordinacionId = :coordinacionId)
            GROUP BY e.condicion
            """)
    List<Object[]> contarPorCondicion(@Param("coordinacionId") Long coordinacionId);

    @Query("""
            SELECT e.categoria.nombre, COUNT(e)
            FROM EquipoJpaEntity e
            WHERE e.activo = TRUE
              AND (:coordinacionId IS NULL OR e.coordinacionId = :coordinacionId)
            GROUP BY e.categoria.nombre
            ORDER BY e.categoria.nombre ASC
            """)
    List<Object[]> contarPorCategoria(@Param("coordinacionId") Long coordinacionId);
}
