package inictel.edu.pe.prestamos.infrastructure.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * Proyeccion de solo lectura sobre la tabla {@code usuario}, con lo justo para
 * mostrar quien presto y quien recibio el bien (RF-24, RF-26).
 */
@Entity
@Immutable
@Table(name = "usuario")
@Getter
@NoArgsConstructor
public class UsuarioReferenciaJpa {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nombres", insertable = false, updatable = false)
    private String nombres;

    @Column(name = "primer_apellido", insertable = false, updatable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido", insertable = false, updatable = false)
    private String segundoApellido;

    /** Nombre completo tal como se muestra en listados y bitacora. */
    public String nombreCompleto() {
        StringBuilder sb = new StringBuilder(nombres).append(' ').append(primerApellido);
        if (segundoApellido != null && !segundoApellido.isBlank()) {
            sb.append(' ').append(segundoApellido);
        }
        return sb.toString();
    }
}
