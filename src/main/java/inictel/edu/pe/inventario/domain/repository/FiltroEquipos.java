package inictel.edu.pe.inventario.domain.repository;

import inictel.edu.pe.inventario.domain.model.CondicionEquipo;

/**
 * Criterios de busqueda del inventario (RF-47, RF-48).
 *
 * <p>Los campos nulos no filtran, con una excepcion deliberada:
 * {@code coordinacionId} lo fija siempre el servicio a partir del usuario en
 * curso, nunca el cliente. Es la llave del aislamiento (RN-23).</p>
 *
 * @param condicion condicion exacta; {@code null} junto a {@code todasLasCondiciones}
 *                  en false significa "solo operativos", que es la vista por
 *                  defecto del listado (RF-47)
 */
public record FiltroEquipos(
        Long coordinacionId,
        String texto,
        Long categoriaId,
        Long laboratorioId,
        CondicionEquipo condicion,
        boolean todasLasCondiciones) {

    /** Vista por defecto: solo los bienes operativos de la coordinacion. */
    public static FiltroEquipos operativosDe(Long coordinacionId) {
        return new FiltroEquipos(coordinacionId, null, null, null, CondicionEquipo.OPERATIVO, false);
    }

    public static FiltroEquipos todosDe(Long coordinacionId) {
        return new FiltroEquipos(coordinacionId, null, null, null, null, true);
    }

    /** Devuelve una copia acotada a la coordinacion indicada. */
    public FiltroEquipos enCoordinacion(Long coordinacion) {
        return new FiltroEquipos(coordinacion, texto, categoriaId, laboratorioId,
                condicion, todasLasCondiciones);
    }
}
