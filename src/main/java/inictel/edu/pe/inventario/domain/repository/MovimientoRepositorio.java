package inictel.edu.pe.inventario.domain.repository;

import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;

import java.util.List;

/**
 * Puerto de persistencia del historial de un bien (RF-53 .. RF-57).
 *
 * <p>Deliberadamente no declara actualizar ni eliminar: el historial es
 * inmutable (RN-21). Lo que no existe en el puerto no puede invocarse desde la
 * aplicacion, y la base lo refuerza con un disparador.</p>
 */
public interface MovimientoRepositorio {

    MovimientoEquipo registrar(MovimientoEquipo movimiento);

    /** Linea de tiempo del bien, del hecho mas reciente al mas antiguo. */
    List<MovimientoEquipo> historialDe(Long equipoId);
}
