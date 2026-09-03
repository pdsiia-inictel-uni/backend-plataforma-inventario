package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.prestamos.domain.model.EstadoPrestamo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data de prestamos. Uso interno del adaptador.
 *
 * <p>En las consultas por coordinacion, {@code coordinacionId} nulo significa
 * "toda la institucion", que es el ambito de lectura del Administrador.</p>
 */
public interface PrestamoJpaRepository
        extends JpaRepository<PrestamoJpaEntity, Long>, JpaSpecificationExecutor<PrestamoJpaEntity> {

    @EntityGraph(attributePaths = {"equipo", "usuarioPresta", "usuarioRecibe"})
    Optional<PrestamoJpaEntity> findWithDetalleById(Long id);

    boolean existsByEquipoIdAndEstado(Long equipoId, EstadoPrestamo estado);

    @EntityGraph(attributePaths = {"equipo", "usuarioPresta", "usuarioRecibe"})
    List<PrestamoJpaEntity> findByEquipoIdOrderByFechaPrestamoDesc(Long equipoId);

    @EntityGraph(attributePaths = {"equipo", "usuarioPresta", "usuarioRecibe"})
    @Query("""
            SELECT p FROM PrestamoJpaEntity p
            WHERE p.dniPersona = :dni
              AND (:coordinacionId IS NULL OR p.coordinacionId = :coordinacionId)
            ORDER BY p.fechaPrestamo DESC
            """)
    List<PrestamoJpaEntity> historialPorPersona(@Param("dni") String dni,
                                                @Param("coordinacionId") Long coordinacionId);

    @Query("""
            SELECT COUNT(p) FROM PrestamoJpaEntity p
            WHERE p.estado = inictel.edu.pe.prestamos.domain.model.EstadoPrestamo.ACTIVO
              AND (:coordinacionId IS NULL OR p.coordinacionId = :coordinacionId)
            """)
    long contarActivos(@Param("coordinacionId") Long coordinacionId);

    @Query("""
            SELECT COUNT(p) FROM PrestamoJpaEntity p
            WHERE p.estado = inictel.edu.pe.prestamos.domain.model.EstadoPrestamo.ACTIVO
              AND p.fechaEstimadaDevolucion IS NOT NULL
              AND p.fechaEstimadaDevolucion < :hoy
              AND (:coordinacionId IS NULL OR p.coordinacionId = :coordinacionId)
            """)
    long contarVencidos(@Param("hoy") LocalDate hoy, @Param("coordinacionId") Long coordinacionId);

    /** RF-77: cuantas salidas registro la propia persona que consulta el panel. */
    @Query("""
            SELECT COUNT(p) FROM PrestamoJpaEntity p
            WHERE p.usuarioPresta.id = :usuarioId
              AND (:coordinacionId IS NULL OR p.coordinacionId = :coordinacionId)
            """)
    long contarRegistradosPor(@Param("usuarioId") Long usuarioId,
                              @Param("coordinacionId") Long coordinacionId);

    @EntityGraph(attributePaths = {"equipo", "usuarioPresta"})
    @Query("""
            SELECT p FROM PrestamoJpaEntity p
            WHERE p.estado = inictel.edu.pe.prestamos.domain.model.EstadoPrestamo.ACTIVO
              AND p.fechaEstimadaDevolucion IS NOT NULL
              AND p.fechaEstimadaDevolucion < :hoy
              AND (:coordinacionId IS NULL OR p.coordinacionId = :coordinacionId)
            ORDER BY p.fechaEstimadaDevolucion ASC
            """)
    List<PrestamoJpaEntity> listarVencidos(@Param("hoy") LocalDate hoy,
                                           @Param("coordinacionId") Long coordinacionId);
}
