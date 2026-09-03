package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.compartido.infrastructure.persistencia.PaginacionJpa;
import inictel.edu.pe.compartido.infrastructure.persistencia.Specs;
import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.Equipo;
import inictel.edu.pe.inventario.domain.repository.EquipoRepositorio;
import inictel.edu.pe.inventario.domain.repository.FiltroEquipos;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Adaptador de persistencia del agregado Bien. */
@Repository
public class EquipoRepositorioJpa implements EquipoRepositorio {

    private static final String ORDEN_POR_DEFECTO = "nombre";

    private final EquipoJpaRepository jpa;
    private final CategoriaJpaRepository categorias;

    public EquipoRepositorioJpa(EquipoJpaRepository jpa, CategoriaJpaRepository categorias) {
        this.jpa = jpa;
        this.categorias = categorias;
    }

    @Override
    public Equipo guardar(Equipo equipo) {
        EquipoJpaEntity existente = equipo.getId() != null
                ? jpa.findById(equipo.getId()).orElse(null)
                : null;

        // La categoria debe ser una referencia gestionada por el contexto de
        // persistencia; el dominio solo transporta su identificador.
        CategoriaJpaEntity categoria = equipo.getCategoria() == null
                ? null
                : categorias.getReferenceById(equipo.getCategoria().id());

        EquipoJpaEntity guardado = jpa.save(InventarioMapper.aEntidad(equipo, existente, categoria));
        equipo.asignarId(guardado.getId());
        return InventarioMapper.aDominio(guardado);
    }

    @Override
    public Optional<Equipo> buscarPorId(Long id) {
        return id == null ? Optional.empty() : jpa.findById(id).map(InventarioMapper::aDominio);
    }

    @Override
    public Pagina<Equipo> buscar(FiltroEquipos filtro, CriterioPagina criterio) {
        return PaginacionJpa.aPagina(
                jpa.findAll(specDe(filtro), PaginacionJpa.aPageable(criterio, ORDEN_POR_DEFECTO)),
                InventarioMapper::aDominio);
    }

    @Override
    public List<Equipo> listar(FiltroEquipos filtro) {
        return jpa.findAll(specDe(filtro), Sort.by(Sort.Direction.ASC, ORDEN_POR_DEFECTO)).stream()
                .map(InventarioMapper::aDominio)
                .toList();
    }

    @Override
    public boolean existeNumeroSerie(String numeroSerie, Long idExcluido) {
        return idExcluido == null
                ? jpa.existsByNumeroSerieIgnoreCase(numeroSerie)
                : jpa.existsByNumeroSerieIgnoreCaseAndIdNot(numeroSerie, idExcluido);
    }

    @Override
    public boolean existeCodigoInventario(String codigo, Long idExcluido) {
        return idExcluido == null
                ? jpa.existsByCodigoInventarioIgnoreCase(codigo)
                : jpa.existsByCodigoInventarioIgnoreCaseAndIdNot(codigo, idExcluido);
    }

    @Override
    public boolean existeCodigoPatrimonial(String codigo, Long idExcluido) {
        return idExcluido == null
                ? jpa.existsByCodigoPatrimonialIgnoreCase(codigo)
                : jpa.existsByCodigoPatrimonialIgnoreCaseAndIdNot(codigo, idExcluido);
    }

    @Override
    public long contarActivosEnCategoria(Long categoriaId) {
        return jpa.countByCategoriaIdAndActivoTrue(categoriaId);
    }

    @Override
    public long contarACargoDe(Long usuarioId) {
        return usuarioId == null ? 0 : jpa.countByResponsableEquipoIdAndActivoTrue(usuarioId);
    }

    @Override
    public List<Equipo> listarACargoDe(Long usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        return jpa.findByResponsableEquipoIdAndActivoTrue(usuarioId).stream()
                .map(InventarioMapper::aDominio)
                .toList();
    }

    @Override
    public List<ConteoACargo> contarPorResponsableDeEquipo(Long coordinacionId) {
        if (coordinacionId == null) {
            return List.of();
        }
        return jpa.contarPorResponsableDeEquipo(coordinacionId).stream()
                .map(fila -> new ConteoACargo((Long) fila[0], ((Number) fila[1]).longValue()))
                .toList();
    }

    @Override
    public long contarActivosEnCoordinacion(Long coordinacionId) {
        return coordinacionId == null ? 0 : jpa.countByCoordinacionIdAndActivoTrue(coordinacionId);
    }

    @Override
    public long contarEnLaboratorio(Long laboratorioId) {
        return laboratorioId == null ? 0 : jpa.countByLaboratorioId(laboratorioId);
    }

    @Override
    public Map<CondicionEquipo, Long> contarPorCondicion(Long coordinacionId) {
        Map<CondicionEquipo, Long> resultado = new LinkedHashMap<>();
        for (Object[] fila : jpa.contarPorCondicion(coordinacionId)) {
            resultado.put((CondicionEquipo) fila[0], ((Number) fila[1]).longValue());
        }
        return resultado;
    }

    @Override
    public Map<String, Long> contarPorCategoria(Long coordinacionId) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (Object[] fila : jpa.contarPorCategoria(coordinacionId)) {
            resultado.put((String) fila[0], ((Number) fila[1]).longValue());
        }
        return resultado;
    }

    @Override
    public long contarRevisionPendiente(Long coordinacionId) {
        return coordinacionId == null
                ? jpa.countByRevisionPendienteTrue()
                : jpa.countByCoordinacionIdAndRevisionPendienteTrue(coordinacionId);
    }

    private Specification<EquipoJpaEntity> specDe(FiltroEquipos filtro) {
        return Specs.combinar(
                EquipoSpecifications.deCoordinacion(filtro.coordinacionId()),
                EquipoSpecifications.textoLibre(filtro.texto()),
                EquipoSpecifications.deCategoria(filtro.categoriaId()),
                EquipoSpecifications.enLaboratorio(filtro.laboratorioId()),
                EquipoSpecifications.aCargoDe(filtro.responsableEquipoId(), filtro.sinResponsableEquipo()),
                EquipoSpecifications.conCondicion(filtro.condicion(), filtro.todasLasCondiciones()));
    }
}
