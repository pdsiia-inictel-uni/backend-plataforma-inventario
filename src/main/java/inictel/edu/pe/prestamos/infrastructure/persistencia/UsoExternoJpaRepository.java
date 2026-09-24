package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.prestamos.domain.model.EstadoUso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repositorio Spring Data del uso externo. Uso interno del adaptador. */
public interface UsoExternoJpaRepository extends JpaRepository<UsoExternoJpaEntity, Long> {

    @EntityGraph(attributePaths = {"equipo", "usuarioRegistra", "usuarioCierra"})
    Optional<UsoExternoJpaEntity> findWithDetalleById(Long id);

    @EntityGraph(attributePaths = {"equipo", "usuarioRegistra", "usuarioCierra"})
    List<UsoExternoJpaEntity> findByEquipoIdOrderByFechaInicioDesc(Long equipoId);

    boolean existsByEquipoIdAndEstado(Long equipoId, EstadoUso estado);
}
