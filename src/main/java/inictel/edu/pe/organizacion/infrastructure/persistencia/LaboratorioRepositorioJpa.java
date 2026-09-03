package inictel.edu.pe.organizacion.infrastructure.persistencia;

import inictel.edu.pe.organizacion.domain.model.Laboratorio;
import inictel.edu.pe.organizacion.domain.repository.LaboratorioRepositorio;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador de persistencia del agregado Laboratorio. */
@Repository
public class LaboratorioRepositorioJpa implements LaboratorioRepositorio {

    private final LaboratorioJpaRepository jpa;

    public LaboratorioRepositorioJpa(LaboratorioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Laboratorio guardar(Laboratorio laboratorio) {
        LaboratorioJpaEntity existente = laboratorio.getId() != null
                ? jpa.findById(laboratorio.getId()).orElse(null)
                : null;
        LaboratorioJpaEntity guardado = jpa.save(OrganizacionMapper.aEntidad(laboratorio, existente));
        laboratorio.asignarId(guardado.getId());
        return OrganizacionMapper.aDominio(guardado);
    }

    @Override
    public Optional<Laboratorio> buscarPorId(Long id) {
        return id == null ? Optional.empty() : jpa.findById(id).map(OrganizacionMapper::aDominio);
    }

    @Override
    public List<Laboratorio> listarPorCoordinacion(Long coordinacionId, boolean soloActivos) {
        List<LaboratorioJpaEntity> entidades = soloActivos
                ? jpa.findByCoordinacionIdAndActivoTrueOrderByNombreAsc(coordinacionId)
                : jpa.findByCoordinacionIdOrderByNombreAsc(coordinacionId);
        return entidades.stream().map(OrganizacionMapper::aDominio).toList();
    }

    @Override
    public boolean existeNombreEnCoordinacion(Long coordinacionId, String nombre, Long idActual) {
        return idActual == null
                ? jpa.existsByCoordinacionIdAndNombreIgnoreCase(coordinacionId, nombre)
                : jpa.existsByCoordinacionIdAndNombreIgnoreCaseAndIdNot(coordinacionId, nombre, idActual);
    }

    @Override
    public long contarActivosPorCoordinacion(Long coordinacionId) {
        return jpa.countByCoordinacionIdAndActivoTrue(coordinacionId);
    }
}
