package inictel.edu.pe.iam.domain.service;

/**
 * Puerto hacia {@code inventario}: lo minimo que la gestion de personas
 * necesita saber de los bienes (RNF-39).
 *
 * <p>Existe por una sola regla, RN-38: <b>quien tiene equipos a su cargo no
 * deja su puesto sin antes soltarlos</b>. Sin ella, dar de baja a un Operador
 * dejaria equipos a nombre de alguien que ya no trabaja en la Coordinacion, y
 * el inventario diria que responde por ellos una persona que se fue.</p>
 *
 * <p>Se declara en el dominio de {@code iam} y lo implementa un adaptador de
 * su infraestructura, que es quien conoce al otro contexto: la regla es de
 * las personas, el dato es de los bienes.</p>
 */
public interface BienesACargo {

    /** Bienes en servicio a nombre de esa persona. */
    long contarDe(Long usuarioId);

    /**
     * Suelta los bienes que esa persona tenia a su nombre, que pasan a cargo
     * del Responsable de la Coordinacion.
     *
     * <p>Se usa en un solo sitio: cuando un Operador <b>asciende</b> a
     * Responsable de su propia Coordinacion (RF-26b, RN-35). Los equipos los
     * sigue llevando la misma persona; lo que cambia es el titulo con el que
     * los lleva.</p>
     */
    void devolverAlResponsableLosDe(Long usuarioId);
}
