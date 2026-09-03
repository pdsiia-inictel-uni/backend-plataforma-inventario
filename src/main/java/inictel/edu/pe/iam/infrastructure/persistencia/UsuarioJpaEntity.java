package inictel.edu.pe.iam.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Representacion persistente del usuario. Es un detalle de infraestructura:
 * el modelo de dominio {@link inictel.edu.pe.iam.domain.model.Usuario} no la
 * conoce y se traduce mediante {@link UsuarioMapper}.
 *
 * <p>Conserva el mapeo a la tabla {@code usuario} definida en la migracion V1,
 * de modo que la reestructuracion no altera la base de datos.</p>
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    @Column(name = "segundo_apellido", nullable = false, length = 100)
    private String segundoApellido;

    @Column(name = "dni", nullable = false, length = 8)
    private String dni;

    @Column(name = "cargo", length = 150)
    private String cargo;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    /** Hash BCrypt; nunca se almacena la contrasena en texto plano (RF-05, RNF-03). */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** RF-16b: null mientras la persona esta registrada pero sin asignar. */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    @Column(name = "debe_cambiar_password", nullable = false)
    private boolean debeCambiarPassword = false;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    /**
     * RF-28d: coordinaciones en las que la persona tiene asignacion.
     *
     * <p>Se carga con el usuario porque casi siempre se necesita: el filtro
     * JWT la usa en cada peticion para resolver el aislamiento. El
     * {@code BatchSize} evita el N+1 de los listados (RNF-16): una sola
     * consulta adicional resuelve la pagina entera.</p>
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_coordinacion", joinColumns = @JoinColumn(name = "usuario_id"))
    @BatchSize(size = 50)
    private Set<AsignacionEmbebida> asignaciones = new LinkedHashSet<>();

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
