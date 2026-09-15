package inictel.edu.pe.organizacion.application.dto;

import inictel.edu.pe.organizacion.domain.model.Laboratorio;
import inictel.edu.pe.organizacion.domain.service.CensoDeBienes;

/**
 * Laboratorio con sus bienes contados por condicion (RF-12, RF-14).
 *
 * @param bienesUbicados todos los bienes ubicados en el, dados de baja incluidos
 * @param bienesVigentes los que siguen en servicio —operativos, prestados y en
 *                       mantenimiento—
 */
public record LaboratorioDto(
        Long id,
        Long coordinacionId,
        String nombre,
        String ubicacion,
        long bienesUbicados,
        long bienesVigentes,
        long bienesOperativos,
        long bienesPrestados,
        long bienesEnMantenimiento,
        long bienesDadosDeBaja) {

    public static LaboratorioDto de(Laboratorio laboratorio, CensoDeBienes.ResumenBienes bienes) {
        return new LaboratorioDto(
                laboratorio.getId(),
                laboratorio.getCoordinacionId(),
                laboratorio.getNombre(),
                laboratorio.getUbicacion(),
                bienes.total(),
                bienes.vigentes(),
                bienes.operativos(),
                bienes.prestados(),
                bienes.enMantenimiento(),
                bienes.dadosDeBaja());
    }
}
