package inictel.edu.pe.inventario.domain.service;

import java.util.Optional;

/**
 * Puerto hacia {@code iam}: lo minimo que el inventario necesita saber de las
 * personas (RNF-39).
 */
public interface DirectorioUsuarios {

    /** Nombre completo de un usuario, para mostrar autorias en la ficha del bien. */
    Optional<String> nombreDe(Long usuarioId);

    /**
     * RF-36: Responsable vigente de una Coordinacion.
     *
     * <p>Se consulta al registrar cada bien para sellarlo con quien esta a cargo
     * en ese momento, sea quien sea quien complete el formulario.</p>
     */
    Optional<Long> responsableVigenteDe(Long coordinacionId);

    /**
     * RF-83: la persona es un Operador <b>activo</b> de esa Coordinacion.
     *
     * <p>Es la unica clase de persona a la que se le puede entregar un bien:
     * el Responsable ya responde por todos los suyos sin necesidad de que se
     * los asignen uno a uno, y nadie de fuera de la Coordinacion tiene por que
     * tener un equipo de ella a su nombre (RN-37, RN-23).</p>
     */
    boolean esOperadorActivoDe(Long usuarioId, Long coordinacionId);
}
