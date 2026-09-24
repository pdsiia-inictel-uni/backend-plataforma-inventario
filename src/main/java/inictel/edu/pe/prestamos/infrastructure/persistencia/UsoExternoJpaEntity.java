package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.prestamos.domain.model.EstadoUso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Representacion persistente del uso externo (tabla {@code uso_externo}). */
@Entity
@Table(name = "uso_externo")
@Getter
@Setter
@NoArgsConstructor
public class UsoExternoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private BienReferenciaJpa equipo;

    /** Coordinacion heredada del bien; la base lo garantiza con una llave compuesta. */
    @Column(name = "coordinacion_id", nullable = false)
    private Long coordinacionId;

    @Column(name = "encargado_nombre", nullable = false, length = 150)
    private String encargadoNombre;

    @Column(name = "encargado_correo", length = 150)
    private String encargadoCorreo;

    @Column(name = "encargado_celular", length = 30)
    private String encargadoCelular;

    @Column(name = "estado_equipo_inicio", nullable = false, length = 40)
    private String estadoEquipoInicio;

    @Column(name = "usuario_nombre", nullable = false, length = 150)
    private String usuarioNombre;

    @Column(name = "usuario_correo", length = 150)
    private String usuarioCorreo;

    @Column(name = "usuario_telefono", length = 30)
    private String usuarioTelefono;

    @Column(name = "proyecto", length = 1000)
    private String proyecto;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin_prevista")
    private LocalDate fechaFinPrevista;

    @Column(name = "hora_fin_prevista")
    private LocalTime horaFinPrevista;

    @Column(name = "actividad", length = 1000)
    private String actividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_registra_id", nullable = false)
    private UsuarioReferenciaJpa usuarioRegistra;

    @Column(name = "entregado_operativo")
    private Boolean entregadoOperativo;

    @Column(name = "devuelto_operativo")
    private Boolean devueltoOperativo;

    @Column(name = "incidente", length = 1000)
    private String incidente;

    @Column(name = "accion_correctiva", length = 1000)
    private String accionCorrectiva;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierra_id")
    private UsuarioReferenciaJpa usuarioCierra;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUso estado = EstadoUso.ABIERTO;
}
