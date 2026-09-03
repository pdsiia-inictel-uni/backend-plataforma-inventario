package inictel.edu.pe.inventario.domain.repository;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;

/**
 * Criterios de busqueda del inventario (RF-47, RF-48, RF-84).
 *
 * <p>Los campos nulos no filtran, con una excepcion deliberada:
 * {@code coordinacionId} lo fija siempre el servicio a partir del usuario en
 * curso, nunca el cliente. Es la llave del aislamiento (RN-23).</p>
 *
 * @param condicion            condicion exacta; {@code null} junto a
 *                             {@code todasLasCondiciones} en false significa
 *                             "solo operativos", que es la vista por defecto
 *                             del listado (RF-47)
 * @param responsableEquipoId  RF-84: deja solo los bienes que esa persona tiene
 *                             a su nombre. Es lo que el Operador usa para ver
 *                             <i>sus</i> equipos y el Responsable para ver los
 *                             de cada uno de los suyos (RF-83)
 * @param sinResponsableEquipo RF-84: deja solo los bienes que no estan
 *                             asignados a ningun Operador, es decir, los que
 *                             lleva el propio Responsable de la Coordinacion,
 *                             que es lo que significa no tener asignacion
 *                             (RN-37). Se ignora si viene un
 *                             {@code responsableEquipoId}: son la misma
 *                             pregunta hecha de dos maneras incompatibles
 */
public record FiltroEquipos(
        Long coordinacionId,
        String texto,
        Long categoriaId,
        Long laboratorioId,
        CondicionEquipo condicion,
        boolean todasLasCondiciones,
        Long responsableEquipoId,
        boolean sinResponsableEquipo) {

    /** Vista por defecto: solo los bienes operativos de la coordinacion. */
    public static FiltroEquipos operativosDe(Long coordinacionId) {
        return new FiltroEquipos(coordinacionId, null, null, null,
                CondicionEquipo.OPERATIVO, false, null, false);
    }

    public static FiltroEquipos todosDe(Long coordinacionId) {
        return new FiltroEquipos(coordinacionId, null, null, null, null, true, null, false);
    }

    /** RF-84: los bienes en servicio a nombre de una persona. */
    public static FiltroEquipos aCargoDe(Long coordinacionId, Long responsableEquipoId) {
        return new FiltroEquipos(coordinacionId, null, null, null, null, true,
                responsableEquipoId, false);
    }

    /** Devuelve una copia acotada a la coordinacion indicada. */
    public FiltroEquipos enCoordinacion(Long coordinacion) {
        return new FiltroEquipos(coordinacion, texto, categoriaId, laboratorioId,
                condicion, todasLasCondiciones, responsableEquipoId, sinResponsableEquipo);
    }

    /** true si la vista esta acotada a quien tiene los bienes a su cargo (RF-84). */
    public boolean filtraPorResponsableDeEquipo() {
        return responsableEquipoId != null || sinResponsableEquipo;
    }
}
