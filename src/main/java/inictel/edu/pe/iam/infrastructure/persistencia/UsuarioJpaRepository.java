package inictel.edu.pe.iam.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data sobre la entidad persistente. Uso interno del adaptador.
 *
 * <p>Las consultas por Coordinacion recorren la tabla de asignaciones
 * ({@code usuario_coordinacion}) desde la v3.3: la adscripcion dejo de ser una
 * columna del usuario cuando un Responsable pudo estar a cargo de varias
 * (RN-05).</p>
 */
public interface UsuarioJpaRepository
        extends JpaRepository<UsuarioJpaEntity, Long>, JpaSpecificationExecutor<UsuarioJpaEntity> {

    /** RF-01: el ingreso admite nombre de usuario o correo institucional. */
    @Query("""
            SELECT u FROM UsuarioJpaEntity u
            WHERE LOWER(u.username) = LOWER(:identificador)
               OR LOWER(u.correo)   = LOWER(:identificador)
            """)
    Optional<UsuarioJpaEntity> buscarPorUsernameOCorreo(@Param("identificador") String identificador);

    /** RF-24: el DNI identifica a una persona en todo el sistema. */
    Optional<UsuarioJpaEntity> findByDni(String dni);

    /**
     * RN-06: como maximo uno; el indice unico parcial de la base lo garantiza.
     *
     * <p>La cuenta de baja no aparece nunca por aqui, porque la baja libera el
     * puesto en el mismo acto (RN-34).</p>
     */
    @Query("""
            SELECT u FROM UsuarioJpaEntity u JOIN u.asignaciones a
            WHERE a.coordinacionId = :coordinacionId
              AND a.rol = :rol
              AND u.estado <> inictel.edu.pe.iam.domain.model.EstadoCuenta.BAJA
            """)
    Optional<UsuarioJpaEntity> buscarPorCoordinacionYRol(@Param("coordinacionId") Long coordinacionId,
                                                         @Param("rol") Rol rol);

    /** RF-29: integrantes de una Coordinacion, el responsable primero. */
    @Query("""
            SELECT u FROM UsuarioJpaEntity u JOIN u.asignaciones a
            WHERE a.coordinacionId = :coordinacionId
              AND (:soloActivos = FALSE OR u.estado = inictel.edu.pe.iam.domain.model.EstadoCuenta.ACTIVA)
            ORDER BY u.rol ASC, u.primerApellido ASC
            """)
    List<UsuarioJpaEntity> listarPorCoordinacion(@Param("coordinacionId") Long coordinacionId,
                                                 @Param("soloActivos") boolean soloActivos);

    /** RF-15: gente de la Coordinacion. La de baja no cuenta: ya dejo su puesto (RN-34). */
    @Query("""
            SELECT COUNT(u) FROM UsuarioJpaEntity u JOIN u.asignaciones a
            WHERE a.coordinacionId = :coordinacionId
              AND a.rol = :rol
              AND u.estado <> inictel.edu.pe.iam.domain.model.EstadoCuenta.BAJA
            """)
    long contarPorCoordinacionYRol(@Param("coordinacionId") Long coordinacionId, @Param("rol") Rol rol);

    @Query("""
            SELECT COUNT(u) FROM UsuarioJpaEntity u JOIN u.asignaciones a
            WHERE a.coordinacionId = :coordinacionId
              AND u.estado <> inictel.edu.pe.iam.domain.model.EstadoCuenta.BAJA
            """)
    long contarActivosEnCoordinacion(@Param("coordinacionId") Long coordinacionId);

    /**
     * RF-26b: personas que pueden tomar el puesto de Responsable de una
     * Coordinacion.
     *
     * <p>Son tres y ni una mas (RN-35): quien no tiene puesto, el Operador
     * <b>de esa misma</b> Coordinacion y un Administrador. Queda fuera quien ya
     * es Responsable —de esa o de cualquier otra—, porque nadie responde por
     * dos inventarios, y quien no puede entrar al sistema.</p>
     */
    @Query("""
            SELECT DISTINCT u FROM UsuarioJpaEntity u LEFT JOIN u.asignaciones a
            WHERE u.estado = inictel.edu.pe.iam.domain.model.EstadoCuenta.ACTIVA
              AND (u.rol IS NULL
                   OR u.rol = inictel.edu.pe.compartido.domain.seguridad.Rol.ADMIN
                   OR (u.rol = inictel.edu.pe.compartido.domain.seguridad.Rol.OPERADOR
                       AND a.coordinacionId = :coordinacionId))
            ORDER BY u.primerApellido ASC, u.nombres ASC
            """)
    List<UsuarioJpaEntity> candidatosAResponsable(@Param("coordinacionId") Long coordinacionId);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);

    long countByRolAndEstado(Rol rol, EstadoCuenta estado);

    long countByEstado(EstadoCuenta estado);
}
