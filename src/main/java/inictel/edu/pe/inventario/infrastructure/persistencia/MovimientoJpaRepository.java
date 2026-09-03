package inictel.edu.pe.inventario.infrastructure.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data del historial. Uso interno del adaptador.
 *
 * <p>Hereda {@code save} y {@code delete} de {@link JpaRepository}, pero el
 * puerto del dominio no los expone y ningun servicio los invoca; ademas la base
 * rechaza cualquier UPDATE o DELETE sobre esta tabla (RN-21).</p>
 */
public interface MovimientoJpaRepository extends JpaRepository<MovimientoJpaEntity, Long> {

    List<MovimientoJpaEntity> findByEquipoIdOrderByFechaHoraDescIdDesc(Long equipoId);
}
