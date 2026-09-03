package inictel.edu.pe.organizacion.domain.repository;

import inictel.edu.pe.organizacion.domain.model.Coordinacion;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Coordinacion. */
public interface CoordinacionRepositorio {

    Coordinacion guardar(Coordinacion coordinacion);

    Optional<Coordinacion> buscarPorId(Long id);

    List<Coordinacion> listar(boolean soloActivas);

    List<Coordinacion> listarPorDireccion(Long direccionId, boolean soloActivas);

    boolean existeNombreEnDireccion(Long direccionId, String nombre, Long idActual);


    /** RF-17: sin al menos una Coordinacion no puede crearse personal operativo. */
    boolean existeAlguna();
}
