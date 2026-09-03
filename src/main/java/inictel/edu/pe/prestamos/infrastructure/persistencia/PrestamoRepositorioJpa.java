package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.infrastructure.persistencia.PaginacionJpa;
import inictel.edu.pe.compartido.infrastructure.persistencia.Specs;
import inictel.edu.pe.prestamos.domain.model.EstadoPrestamo;
import inictel.edu.pe.prestamos.domain.model.OperadorPrestamo;
import inictel.edu.pe.prestamos.domain.model.Prestamo;
import inictel.edu.pe.prestamos.domain.repository.FiltroPrestamos;
import inictel.edu.pe.prestamos.domain.repository.PrestamoRepositorio;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Adaptador de persistencia del agregado Prestamo. */
@Repository
public class PrestamoRepositorioJpa implements PrestamoRepositorio {

    private static final String ORDEN_POR_DEFECTO = "fechaPrestamo";

    private final PrestamoJpaRepository jpa;
    private final EntityManager entityManager;

    public PrestamoRepositorioJpa(PrestamoJpaRepository jpa, EntityManager entityManager) {
        this.jpa = jpa;
        this.entityManager = entityManager;
    }

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        PrestamoJpaEntity existente = prestamo.getId() != null
                ? jpa.findWithDetalleById(prestamo.getId()).orElse(null)
                : null;

        BienReferenciaJpa bien = referenciaBien(prestamo.getBien().id());
        UsuarioReferenciaJpa presta = referenciaUsuario(prestamo.getUsuarioPresta());
        UsuarioReferenciaJpa recibe = referenciaUsuario(prestamo.getUsuarioRecibe());

        PrestamoJpaEntity guardado = jpa.save(
                PrestamoMapper.aEntidad(prestamo, existente, bien, presta, recibe));
        prestamo.asignarId(guardado.getId());
        return PrestamoMapper.aDominio(guardado);
    }

    @Override
    public Optional<Prestamo> buscarPorId(Long id) {
        return jpa.findWithDetalleById(id).map(PrestamoMapper::aDominio);
    }

    @Override
    public Pagina<Prestamo> buscar(FiltroPrestamos filtro, CriterioPagina criterio) {
        Specification<PrestamoJpaEntity> spec = Specs.combinar(
                PrestamoSpecifications.deCoordinacion(filtro.coordinacionId()),
                PrestamoSpecifications.textoLibre(filtro.texto()),
                PrestamoSpecifications.conEstado(filtro.estado()),
                PrestamoSpecifications.deEquipo(filtro.equipoId()),
                PrestamoSpecifications.conDni(filtro.dni()),
                PrestamoSpecifications.desde(filtro.desde()),
                PrestamoSpecifications.hasta(filtro.hasta()),
                PrestamoSpecifications.soloVencidos(filtro.soloVencidos()));

        return PaginacionJpa.aPagina(
                jpa.findAll(spec, PaginacionJpa.aPageable(criterio, ORDEN_POR_DEFECTO)),
                PrestamoMapper::aDominio);
    }

    @Override
    public List<Prestamo> historialPorBien(Long equipoId) {
        return jpa.findByEquipoIdOrderByFechaPrestamoDesc(equipoId).stream()
                .map(PrestamoMapper::aDominio).toList();
    }

    @Override
    public List<Prestamo> historialPorPersona(String dni, Long coordinacionId) {
        return jpa.historialPorPersona(dni, coordinacionId).stream()
                .map(PrestamoMapper::aDominio).toList();
    }

    @Override
    public List<Prestamo> vencidos(Long coordinacionId) {
        return jpa.listarVencidos(LocalDate.now(), coordinacionId).stream()
                .map(PrestamoMapper::aDominio).toList();
    }

    @Override
    public boolean tienePrestamoActivo(Long equipoId) {
        return jpa.existsByEquipoIdAndEstado(equipoId, EstadoPrestamo.ACTIVO);
    }

    @Override
    public long contarActivos(Long coordinacionId) {
        return jpa.contarActivos(coordinacionId);
    }

    @Override
    public long contarVencidos(Long coordinacionId) {
        return jpa.contarVencidos(LocalDate.now(), coordinacionId);
    }

    @Override
    public long contarRegistradosPor(Long usuarioId, Long coordinacionId) {
        return usuarioId == null ? 0 : jpa.contarRegistradosPor(usuarioId, coordinacionId);
    }

    /**
     * Referencia perezosa a la fila de otro contexto: basta el identificador,
     * no hace falta cargar la entidad completa para guardar la clave foranea.
     */
    private BienReferenciaJpa referenciaBien(Long equipoId) {
        BienReferenciaJpa bien = entityManager.getReference(BienReferenciaJpa.class, equipoId);
        if (bien == null) {
            throw RecursoNoEncontradoException.de("el bien", equipoId);
        }
        return bien;
    }

    private UsuarioReferenciaJpa referenciaUsuario(OperadorPrestamo operador) {
        if (operador == null || operador.id() == null) {
            return null;
        }
        return entityManager.getReference(UsuarioReferenciaJpa.class, operador.id());
    }
}
