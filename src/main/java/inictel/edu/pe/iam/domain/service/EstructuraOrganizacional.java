package inictel.edu.pe.iam.domain.service;

import java.util.Optional;

/**
 * Puerto hacia el contexto {@code organizacion}.
 *
 * <p>Capa anticorrupcion: {@code iam} necesita saber si una Coordinacion existe
 * y esta activa para adscribir usuarios, pero no conoce el agregado
 * Coordinacion ni sus tablas (RNF-39).</p>
 */
public interface EstructuraOrganizacional {

    /**
     * RF-17: sin al menos una Coordinacion no puede crearse personal operativo.
     * Es la primera validacion del alta de un Responsable u Operador.
     */
    boolean existeAlgunaCoordinacion();

    boolean existeCoordinacionActiva(Long coordinacionId);

    /**
     * Nombre de la Coordinacion y de su Direccion, para mensajes, encabezados y
     * el saludo de bienvenida (RF-01b).
     *
     * <p>Las dos juntas porque juntas se muestran: "Coordinacion de Redes" sin
     * su Direccion no dice donde esta parada la persona.</p>
     */
    Optional<Ubicacion> ubicacionDeCoordinacion(Long coordinacionId);

    /** Coordinacion nombrada junto a la Direccion a la que pertenece. */
    record Ubicacion(String coordinacion, String direccion) {
    }
}
