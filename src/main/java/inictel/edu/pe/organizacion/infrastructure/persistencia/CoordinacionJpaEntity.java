package inictel.edu.pe.organizacion.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representacion persistente de la coordinacion (tabla {@code coordinacion}).
 *
 * <p>La relacion con la direccion se guarda como identificador plano y no como
 * {@code @ManyToOne}: el dominio referencia por id y asi se evita arrastrar
 * grafos de objetos innecesarios en las consultas de listado (RNF-16).</p>
 */
@Entity
@Table(name = "coordinacion")
@Getter
@Setter
@NoArgsConstructor
public class CoordinacionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "direccion_id", nullable = false)
    private Long direccionId;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
