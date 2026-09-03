package inictel.edu.pe.inventario.infrastructure.persistencia;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representacion persistente del bien (tabla {@code equipo}).
 *
 * <p>La categoria se mapea como asociacion perezosa porque el listado muestra
 * su nombre; el resto de relaciones se guardan como identificadores planos para
 * no arrastrar grafos innecesarios en las consultas paginadas (RNF-16).</p>
 */
@Entity
@Table(name = "equipo")
@Getter
@Setter
@NoArgsConstructor
public class EquipoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RN-11: duenno del bien y llave del aislamiento. */
    @Column(name = "coordinacion_id", nullable = false)
    private Long coordinacionId;

    /** RN-12: ubicacion fisica opcional, siempre de la misma coordinacion. */
    @Column(name = "laboratorio_id")
    private Long laboratorioId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "marca", nullable = false, length = 100)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    @Column(name = "numero_serie", nullable = false, length = 100)
    private String numeroSerie;

    @Column(name = "codigo_inventario", nullable = false, length = 60)
    private String codigoInventario;

    @Column(name = "codigo_patrimonial", nullable = false, length = 60)
    private String codigoPatrimonial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaJpaEntity categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion", nullable = false, length = 20)
    private CondicionEquipo condicion = CondicionEquipo.OPERATIVO;

    @Column(name = "fecha_adquisicion", nullable = false)
    private LocalDate fechaAdquisicion;

    @Column(name = "costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "foto_url", length = 300)
    private String fotoUrl;

    /** RN-19: devuelto con dano; el Responsable debe decidir. */
    @Column(name = "revision_pendiente", nullable = false)
    private boolean revisionPendiente = false;

    @Column(name = "motivo_baja", length = 500)
    private String motivoBaja;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    /** RF-36: Responsable vigente al momento del alta. */
    @Column(name = "responsable_id")
    private Long responsableId;

    /** RF-83: Operador a cargo del bien. NULL = lo lleva el Responsable. */
    @Column(name = "responsable_equipo_id")
    private Long responsableEquipoId;

    @Column(name = "usuario_registro_id", nullable = false)
    private Long usuarioRegistroId;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
