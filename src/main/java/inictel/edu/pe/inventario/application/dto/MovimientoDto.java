package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;
import inictel.edu.pe.inventario.domain.model.TipoMovimiento;

import java.time.LocalDateTime;

/**
 * Un hecho de la linea de tiempo del bien, listo para leerse (RF-56).
 *
 * <p>{@code autoria} trae la frase ya redactada para que la vista no tenga que
 * componerla y el texto sea el mismo en la pantalla, en la exportacion y en
 * cualquier otro consumidor.</p>
 *
 * @param autoria quien protagoniza el hecho, con su rol. En el <b>alta</b>
 *                lleva el rotulo <i>"Registrado por:"</i>, porque desde la
 *                v3.12 la ficha del bien ya no muestra quien lo registro y el
 *                historial es donde se consulta: sin el rotulo, el nombre
 *                queda suelto detras de la fecha y no dice de que responde
 *                esa persona (RF-56, RNF-29). Los demas hechos ya llevan su
 *                verbo en el titulo —"Prestado", "Dado de baja"— y no
 *                necesitan repetirlo
 */
public record MovimientoDto(
        Long id,
        TipoMovimiento tipo,
        String tipoEtiqueta,
        String condicionAnterior,
        String condicionNueva,
        String detalle,
        String usuario,
        Rol rolUsuario,
        String rolEtiqueta,
        String autoria,
        LocalDateTime fechaHora) {

    /** RNF-47: el historial nombra a quien ya no esta, y a quien no consta. */
    private static final String AUTOR_DESCONOCIDO = "Usuario no disponible";

    public static MovimientoDto de(MovimientoEquipo m) {
        String autor = m.usuarioNombre() == null ? AUTOR_DESCONOCIDO : m.usuarioNombre();
        String rol = m.rolUsuario() == null ? null : m.rolUsuario().getEtiqueta();
        String conRol = rol == null ? autor : autor + " (" + rol + ")";

        return new MovimientoDto(
                m.id(),
                m.tipo(),
                m.tipo().getEtiqueta(),
                m.condicionAnterior() == null ? null : m.condicionAnterior().getEtiqueta(),
                m.condicionNueva() == null ? null : m.condicionNueva().getEtiqueta(),
                m.detalle(),
                autor,
                m.rolUsuario(),
                rol,
                m.tipo() == TipoMovimiento.ALTA ? "Registrado por: " + conRol : conRol,
                m.fechaHora());
    }
}
