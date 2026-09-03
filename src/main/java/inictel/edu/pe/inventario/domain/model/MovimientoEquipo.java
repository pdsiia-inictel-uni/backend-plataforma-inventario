package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;

import java.time.LocalDateTime;

/**
 * Hecho inmutable de la linea de tiempo de un bien (RF-53 .. RF-56).
 *
 * <p>Es un objeto de valor: una vez creado no se modifica ni se elimina, y no
 * expone ningun metodo que permita alterar su fecha. La misma garantia esta
 * replicada en la base de datos con un disparador (RN-21), de modo que ni un
 * error de programacion futuro pueda reescribir la historia.</p>
 *
 * <p>El nombre y el rol del autor se guardan copiados, no referenciados: si esa
 * persona cambia de rol o de coordinacion mas adelante, el historial debe
 * seguir diciendo quien era cuando actuo.</p>
 */
public record MovimientoEquipo(
        Long id,
        Long equipoId,
        TipoMovimiento tipo,
        CondicionEquipo condicionAnterior,
        CondicionEquipo condicionNueva,
        String detalle,
        Long usuarioId,
        String usuarioNombre,
        Rol rolUsuario,
        LocalDateTime fechaHora) {

    private static final int MAX_DETALLE = 1000;

    /**
     * Nuevo movimiento, sellado con la hora del servidor.
     *
     * <p>La fecha no se recibe por parametro a proposito: nadie fuera de este
     * metodo puede decidirla (RN-21).</p>
     */
    public static MovimientoEquipo registrar(Long equipoId,
                                             TipoMovimiento tipo,
                                             CondicionEquipo anterior,
                                             CondicionEquipo nueva,
                                             String detalle,
                                             UsuarioAutenticado autor) {
        return new MovimientoEquipo(
                null,
                equipoId,
                tipo,
                anterior,
                nueva,
                recortar(detalle),
                autor == null ? null : autor.id(),
                autor == null ? null : autor.nombreCompleto(),
                autor == null ? null : autor.rol(),
                LocalDateTime.now());
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static MovimientoEquipo reconstituir(Long id, Long equipoId, TipoMovimiento tipo,
                                                CondicionEquipo anterior, CondicionEquipo nueva,
                                                String detalle, Long usuarioId, String usuarioNombre,
                                                Rol rolUsuario, LocalDateTime fechaHora) {
        return new MovimientoEquipo(id, equipoId, tipo, anterior, nueva, detalle,
                usuarioId, usuarioNombre, rolUsuario, fechaHora);
    }

    /** true si el hecho implico un cambio de condicion del bien. */
    public boolean cambioCondicion() {
        return condicionAnterior != null && condicionNueva != null && condicionAnterior != condicionNueva;
    }

    private static String recortar(String detalle) {
        if (detalle == null) {
            return null;
        }
        String limpio = detalle.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        return limpio.length() > MAX_DETALLE ? limpio.substring(0, MAX_DETALLE) : limpio;
    }
}
