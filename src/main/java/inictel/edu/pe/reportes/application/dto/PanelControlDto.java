package inictel.edu.pe.reportes.application.dto;

import inictel.edu.pe.compartido.domain.seguridad.Rol;

import java.util.List;

/**
 * Indicadores del panel de control (RF-75 .. RF-77).
 *
 * <p>Un unico contrato para los tres paneles. El servicio rellena solo lo que
 * corresponde al rol, y la vista muestra lo que recibe: el Operador no ve
 * cifras que no puede accionar, y el Administrador ve las institucionales.</p>
 *
 * @param ambito             texto del encabezado: "INICTEL-UNI" o el nombre de
 *                           la coordinacion en curso
 * @param sinResponsable     coordinaciones sin responsable asignado; es la
 *                           alerta critica del panel del Administrador (RF-75)
 * @param revisionPendiente  bienes devueltos con dano a la espera de decision
 *                           del Responsable (RF-76)
 */
public record PanelControlDto(
        Rol rol,
        String ambito,
        Long coordinacionId,
        long totalBienes,
        long operativos,
        long prestados,
        long enMantenimiento,
        long dadosDeBaja,
        long revisionPendiente,
        long prestamosActivos,
        long prestamosVencidos,
        long usuarios,
        long direcciones,
        long coordinaciones,
        long laboratorios,
        long sinResponsable,
        List<ConteoDto> porCategoria,
        List<ConteoDto> porCondicion) {

    /** Par etiqueta-cantidad usado por los graficos de barras. */
    public record ConteoDto(String etiqueta, long cantidad) {
    }
}
