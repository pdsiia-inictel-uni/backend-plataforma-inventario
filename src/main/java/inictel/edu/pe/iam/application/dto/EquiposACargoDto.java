package inictel.edu.pe.iam.application.dto;

import java.util.List;

/**
 * Los equipos que retienen a una persona en su puesto (RN-38).
 *
 * <p>Existe para que la pantalla pueda <b>advertirlo antes</b> en vez de
 * dejar que el usuario pulse "Dar de baja" y reciba un error: quien administra
 * personas necesita ver, en la ficha, que este Operador lleva tres equipos y
 * cuales son, para pedirle al Responsable que los reasigne (RNF-23, RNF-26).</p>
 *
 * @param usuarioId       persona consultada
 * @param nombreCompleto  como se la nombra en los avisos (RNF-29)
 * @param cantidad        cuantos bienes en servicio tiene a su nombre
 * @param puedeDarseDeBaja false mientras conserve alguno: la baja se rechazaria
 * @param equipos         los bienes, para que el aviso diga cuales son
 */
public record EquiposACargoDto(
        Long usuarioId,
        String nombreCompleto,
        int cantidad,
        boolean puedeDarseDeBaja,
        List<EquipoACargoDto> equipos) {

    /** Lo justo para reconocer un equipo en la lista del aviso. */
    public record EquipoACargoDto(Long id, String nombre, String codigoInventario, String laboratorio) {
    }
}
