package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;
import inictel.edu.pe.inventario.domain.repository.MovimientoRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Adaptador de persistencia del historial del bien.
 *
 * <p>Solo inserta y lee: no expone actualizacion ni borrado, en coherencia con
 * la inmutabilidad del historial (RN-21).</p>
 */
@Repository
public class MovimientoRepositorioJpa implements MovimientoRepositorio {

    private final MovimientoJpaRepository jpa;

    public MovimientoRepositorioJpa(MovimientoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public MovimientoEquipo registrar(MovimientoEquipo movimiento) {
        return InventarioMapper.aDominio(jpa.save(InventarioMapper.aEntidad(movimiento)));
    }

    @Override
    public List<MovimientoEquipo> historialDe(Long equipoId) {
        return jpa.findByEquipoIdOrderByFechaHoraDescIdDesc(equipoId).stream()
                .map(InventarioMapper::aDominio)
                .toList();
    }
}
