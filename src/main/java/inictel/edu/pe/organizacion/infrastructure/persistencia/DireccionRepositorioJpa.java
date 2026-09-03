package inictel.edu.pe.organizacion.infrastructure.persistencia;

import inictel.edu.pe.organizacion.domain.model.Direccion;
import inictel.edu.pe.organizacion.domain.repository.DireccionRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador de persistencia del agregado Direccion. */
@Repository
public class DireccionRepositorioJpa implements DireccionRepositorio {

    private final DireccionJpaRepository jpa;

    public DireccionRepositorioJpa(DireccionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Direccion guardar(Direccion direccion) {
        DireccionJpaEntity existente = direccion.getId() != null
                ? jpa.findById(direccion.getId()).orElse(null)
                : null;
        DireccionJpaEntity guardada = jpa.save(OrganizacionMapper.aEntidad(direccion, existente));
        direccion.asignarId(guardada.getId());
        return OrganizacionMapper.aDominio(guardada);
    }

    @Override
    public Optional<Direccion> buscarPorId(Long id) {
        return id == null ? Optional.empty() : jpa.findById(id).map(OrganizacionMapper::aDominio);
    }

    @Override
    public List<Direccion> listar(boolean soloActivas) {
        List<DireccionJpaEntity> entidades = soloActivas
                ? jpa.findByActivaTrueOrderByNombreAsc()
                : jpa.findAllByOrderByNombreAsc();
        return entidades.stream().map(OrganizacionMapper::aDominio).toList();
    }

    @Override
    public boolean existeNombre(String nombre, Long idActual) {
        return idActual == null
                ? jpa.existsByNombreIgnoreCase(nombre)
                : jpa.existsByNombreIgnoreCaseAndIdNot(nombre, idActual);
    }
}
