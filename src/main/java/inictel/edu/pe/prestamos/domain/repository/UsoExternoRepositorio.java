package inictel.edu.pe.prestamos.domain.repository;

import inictel.edu.pe.prestamos.domain.model.UsoExterno;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Uso externo (RF-78). */
public interface UsoExternoRepositorio {

    UsoExterno guardar(UsoExterno uso);

    Optional<UsoExterno> buscarPorId(Long id);

    /** Historial de usos externos de un bien, el mas reciente primero. */
    List<UsoExterno> historialPorBien(Long equipoId);

    boolean tieneAbierto(Long equipoId);

    /** Borra un uso anulado antes de usarse el equipo. */
    void eliminar(Long id);
}
