package inictel.edu.pe.organizacion.infrastructure.persistencia;

import inictel.edu.pe.organizacion.domain.model.Coordinacion;
import inictel.edu.pe.organizacion.domain.repository.CoordinacionRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador de persistencia del agregado Coordinacion. */
@Repository
public class CoordinacionRepositorioJpa implements CoordinacionRepositorio {

    private final CoordinacionJpaRepository jpa;

    public CoordinacionRepositorioJpa(CoordinacionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Coordinacion guardar(Coordinacion coordinacion) {
        CoordinacionJpaEntity existente = coordinacion.getId() != null
                ? jpa.findById(coordinacion.getId()).orElse(null)
                : null;
        CoordinacionJpaEntity guardada = jpa.save(OrganizacionMapper.aEntidad(coordinacion, existente));
        coordinacion.asignarId(guardada.getId());
        return OrganizacionMapper.aDominio(guardada);
    }

    @Override
    public Optional<Coordinacion> buscarPorId(Long id) {
        return id == null ? Optional.empty() : jpa.findById(id).map(OrganizacionMapper::aDominio);
    }

    @Override
    public List<Coordinacion> listar(boolean soloActivas) {
        List<CoordinacionJpaEntity> entidades = soloActivas
                ? jpa.findByActivaTrueOrderByNombreAsc()
                : jpa.findAllByOrderByNombreAsc();
        return entidades.stream().map(OrganizacionMapper::aDominio).toList();
    }

    @Override
    public List<Coordinacion> listarPorDireccion(Long direccionId, boolean soloActivas) {
        List<CoordinacionJpaEntity> entidades = soloActivas
                ? jpa.findByDireccionIdAndActivaTrueOrderByNombreAsc(direccionId)
                : jpa.findByDireccionIdOrderByNombreAsc(direccionId);
        return entidades.stream().map(OrganizacionMapper::aDominio).toList();
    }

    @Override
    public boolean existeNombreEnDireccion(Long direccionId, String nombre, Long idActual) {
        return idActual == null
                ? jpa.existsByDireccionIdAndNombreIgnoreCase(direccionId, nombre)
                : jpa.existsByDireccionIdAndNombreIgnoreCaseAndIdNot(direccionId, nombre, idActual);
    }

    @Override
    public boolean existeAlguna() {
        return jpa.count() > 0;
    }
}
