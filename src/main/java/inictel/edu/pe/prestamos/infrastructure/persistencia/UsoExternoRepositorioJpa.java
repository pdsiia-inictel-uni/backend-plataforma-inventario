package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.prestamos.domain.model.BienPrestado;
import inictel.edu.pe.prestamos.domain.model.EstadoUso;
import inictel.edu.pe.prestamos.domain.model.OperadorPrestamo;
import inictel.edu.pe.prestamos.domain.model.UsoExterno;
import inictel.edu.pe.prestamos.domain.repository.UsoExternoRepositorio;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adaptador de persistencia del agregado Uso externo. */
@Repository
public class UsoExternoRepositorioJpa implements UsoExternoRepositorio {

    private final UsoExternoJpaRepository jpa;
    private final EntityManager entityManager;

    public UsoExternoRepositorioJpa(UsoExternoJpaRepository jpa, EntityManager entityManager) {
        this.jpa = jpa;
        this.entityManager = entityManager;
    }

    @Override
    public UsoExterno guardar(UsoExterno uso) {
        UsoExternoJpaEntity entidad = uso.getId() != null
                ? jpa.findWithDetalleById(uso.getId()).orElseGet(UsoExternoJpaEntity::new)
                : new UsoExternoJpaEntity();

        entidad.setId(uso.getId());
        entidad.setEquipo(entityManager.getReference(BienReferenciaJpa.class, uso.getBien().id()));
        entidad.setCoordinacionId(uso.coordinacionId());
        entidad.setEncargadoNombre(uso.getEncargadoNombre());
        entidad.setEncargadoCorreo(uso.getEncargadoCorreo());
        entidad.setEncargadoCelular(uso.getEncargadoCelular());
        entidad.setEstadoEquipoInicio(uso.getEstadoEquipoInicio());
        entidad.setUsuarioNombre(uso.getUsuarioNombre());
        entidad.setUsuarioCorreo(uso.getUsuarioCorreo());
        entidad.setUsuarioTelefono(uso.getUsuarioTelefono());
        entidad.setProyecto(uso.getProyecto());
        entidad.setFechaInicio(uso.getFechaInicio());
        entidad.setFechaFinPrevista(uso.getFechaFinPrevista());
        entidad.setHoraFinPrevista(uso.getHoraFinPrevista());
        entidad.setActividad(uso.getActividad());
        entidad.setUsuarioRegistra(referencia(uso.getRegistradoPor()));
        entidad.setEntregadoOperativo(uso.getEntregadoOperativo());
        entidad.setDevueltoOperativo(uso.getDevueltoOperativo());
        entidad.setIncidente(uso.getIncidente());
        entidad.setAccionCorrectiva(uso.getAccionCorrectiva());
        entidad.setObservaciones(uso.getObservaciones());
        entidad.setFechaCierre(uso.getFechaCierre());
        entidad.setUsuarioCierra(referencia(uso.getCerradoPor()));
        entidad.setEstado(uso.getEstado());

        UsoExternoJpaEntity guardado = jpa.saveAndFlush(entidad);
        uso.asignarId(guardado.getId());
        return jpa.findWithDetalleById(guardado.getId()).map(this::aDominio).orElse(uso);
    }

    @Override
    public Optional<UsoExterno> buscarPorId(Long id) {
        return jpa.findWithDetalleById(id).map(this::aDominio);
    }

    @Override
    public List<UsoExterno> historialPorBien(Long equipoId) {
        return jpa.findByEquipoIdOrderByFechaInicioDesc(equipoId).stream().map(this::aDominio).toList();
    }

    @Override
    public boolean tieneAbierto(Long equipoId) {
        return jpa.existsByEquipoIdAndEstado(equipoId, EstadoUso.ABIERTO);
    }

    @Override
    public void eliminar(Long id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private UsuarioReferenciaJpa referencia(OperadorPrestamo operador) {
        return operador == null || operador.id() == null ? null
                : entityManager.getReference(UsuarioReferenciaJpa.class, operador.id());
    }

    private UsoExterno aDominio(UsoExternoJpaEntity e) {
        BienReferenciaJpa bien = e.getEquipo();
        return UsoExterno.reconstituir(
                e.getId(),
                new BienPrestado(bien.getId(), e.getCoordinacionId(), bien.getNombre(),
                        bien.getCodigoInventario(), bien.getNumeroSerie()),
                e.getEstadoEquipoInicio(),
                e.getEncargadoNombre(), e.getEncargadoCorreo(), e.getEncargadoCelular(),
                e.getUsuarioNombre(), e.getUsuarioCorreo(), e.getUsuarioTelefono(),
                e.getProyecto(),
                e.getFechaInicio(), e.getFechaFinPrevista(), e.getHoraFinPrevista(), e.getActividad(),
                operador(e.getUsuarioRegistra()),
                e.getEntregadoOperativo(), e.getDevueltoOperativo(),
                e.getIncidente(), e.getAccionCorrectiva(), e.getObservaciones(),
                e.getFechaCierre(), operador(e.getUsuarioCierra()),
                e.getEstado());
    }

    private OperadorPrestamo operador(UsuarioReferenciaJpa usuario) {
        return usuario == null ? null : new OperadorPrestamo(usuario.getId(), usuario.nombreCompleto());
    }
}
