package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representacion persistente de un movimiento del bien
 * (tabla {@code movimiento_equipo}).
 *
 * <p>Solo se inserta. La aplicacion nunca emite UPDATE ni DELETE sobre esta
 * tabla, y un disparador de la base lo impide igualmente (RN-21).</p>
 */
@Entity
@Table(name = "movimiento_equipo")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipo_id", nullable = false)
    private Long equipoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_anterior", length = 20)
    private CondicionEquipo condicionAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_nueva", length = 20)
    private CondicionEquipo condicionNueva;

    @Column(name = "detalle", length = 1000)
    private String detalle;

    @Column(name = "usuario_id")
    private Long usuarioId;

    /** Copiado, no referenciado: el historial conserva quien era en su momento. */
    @Column(name = "usuario_nombre", length = 200)
    private String usuarioNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_usuario", length = 20)
    private Rol rolUsuario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
}
