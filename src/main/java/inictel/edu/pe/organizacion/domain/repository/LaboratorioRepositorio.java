package inictel.edu.pe.organizacion.domain.repository;

import inictel.edu.pe.organizacion.domain.model.Laboratorio;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Laboratorio. */
public interface LaboratorioRepositorio {

    Laboratorio guardar(Laboratorio laboratorio);

    Optional<Laboratorio> buscarPorId(Long id);

    List<Laboratorio> listarPorCoordinacion(Long coordinacionId, boolean soloActivos);

    boolean existeNombreEnCoordinacion(Long coordinacionId, String nombre, Long idActual);

    long contarActivosPorCoordinacion(Long coordinacionId);
}
