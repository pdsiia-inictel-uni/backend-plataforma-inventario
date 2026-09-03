package inictel.edu.pe.prestamos.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Proyeccion de solo lectura sobre la tabla {@code equipo}.
 *
 * <p>Prestamos comparte base de datos con inventario —es un monolito modular—
 * pero no comparte su modelo: en lugar de reutilizar el agregado ajeno, declara
 * su propia vista inmutable con los campos que necesita mostrar y filtrar.
 * Nunca se escribe a traves de ella; los cambios de condicion del bien se piden
 * al contexto inventario mediante su servicio publico.</p>
 */
@Entity
@Immutable
@Table(name = "equipo")
@Getter
@NoArgsConstructor
public class BienReferenciaJpa {

    @Id
    @Column(name = "id")
    private Long id;

    /** RN-23: permite filtrar los prestamos por coordinacion sin salir del contexto. */
    @Column(name = "coordinacion_id", insertable = false, updatable = false)
    private Long coordinacionId;

    @Column(name = "nombre", insertable = false, updatable = false)
    private String nombre;

    @Column(name = "codigo_inventario", insertable = false, updatable = false)
    private String codigoInventario;

    @Column(name = "numero_serie", insertable = false, updatable = false)
    private String numeroSerie;
}
