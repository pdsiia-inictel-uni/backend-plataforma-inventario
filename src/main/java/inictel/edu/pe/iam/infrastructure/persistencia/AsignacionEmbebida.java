package inictel.edu.pe.iam.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Fila de la tabla {@code usuario_coordinacion}: la asignacion de una persona
 * a una Coordinacion con un rol operativo (RF-28d).
 *
 * <p>El rol se guarda tambien aqui, y no solo en el usuario, porque es lo que
 * permite a la base sostener por si misma la unicidad del RESPONSABLE de cada
 * Coordinacion (RN-06), un indice unico parcial sobre esta tabla. La otra
 * invariante —una persona, una sola Coordinacion (RN-05)— es un indice unico
 * sobre {@code usuario_id}, sin condicion, porque desde la v3.4 vale para los
 * dos roles operativos.</p>
 *
 * <p>{@code fecha_asignacion} no se mapea a proposito: la pone la base al
 * insertar y nadie la modifica despues (RN-21).</p>
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AsignacionEmbebida {

    @Column(name = "coordinacion_id", nullable = false)
    private Long coordinacionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    public AsignacionEmbebida(Long coordinacionId, Rol rol) {
        this.coordinacionId = coordinacionId;
        this.rol = rol;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof AsignacionEmbebida asignacion)) {
            return false;
        }
        return Objects.equals(coordinacionId, asignacion.coordinacionId) && rol == asignacion.rol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinacionId, rol);
    }
}
