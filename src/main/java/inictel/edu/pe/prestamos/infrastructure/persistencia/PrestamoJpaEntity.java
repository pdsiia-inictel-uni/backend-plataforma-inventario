package inictel.edu.pe.prestamos.infrastructure.persistencia;

import inictel.edu.pe.prestamos.domain.model.EstadoPrestamo;
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

/**
 * Representacion persistente del prestamo (tabla {@code prestamo}).
 *
 * <p>Las asociaciones apuntan a proyecciones de solo lectura de las tablas de
 * otros contextos, de modo que este agregado nunca escriba sobre ellas.</p>
 */
@Entity
@Table(name = "prestamo")
@Getter
@Setter
@NoArgsConstructor
public class PrestamoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipo_id", nullable = false)
    private BienReferenciaJpa equipo;

    /**
     * Coordinacion heredada del bien. Se guarda desnormalizada para poder
     * filtrar y contar por coordinacion sin unir con {@code equipo} en cada
     * consulta (RNF-14); la base garantiza que coincide con la del bien
     * mediante una llave foranea compuesta.
     */
    @Column(name = "coordinacion_id", nullable = false)
    private Long coordinacionId;

    @Column(name = "nombre_persona", nullable = false, length = 200)
    private String nombrePersona;

    @Column(name = "dni_persona", nullable = false, length = 8)
    private String dniPersona;

    @Column(name = "destino", length = 200)
    private String destino;

    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_estimada_devolucion")
    private LocalDate fechaEstimadaDevolucion;

    @Column(name = "observaciones_salida", length = 1000)
    private String observacionesSalida;

    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion;

    @Column(name = "conforme")
    private Boolean conforme;

    /** RN-19: devuelto con dano; marca el bien para revision del Responsable. */
    @Column(name = "reporta_dano", nullable = false)
    private boolean reportaDano = false;

    @Column(name = "observaciones_retorno", length = 1000)
    private String observacionesRetorno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_presta_id", nullable = false)
    private UsuarioReferenciaJpa usuarioPresta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_recibe_id")
    private UsuarioReferenciaJpa usuarioRecibe;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;
}
