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

/** Representacion persistente de la direccion (tabla {@code direccion}). */
@Entity
@Table(name = "direccion")
@Getter
@Setter
@NoArgsConstructor
public class DireccionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "sigla", length = 20)
    private String sigla;
}
