package inictel.edu.pe.organizacion.application.dto;

import inictel.edu.pe.organizacion.domain.model.Laboratorio;

public record LaboratorioDto(
        Long id,
        Long coordinacionId,
        String nombre,
        String ubicacion,
        boolean activo,
        long bienesUbicados) {

    public static LaboratorioDto de(Laboratorio laboratorio, long bienesUbicados) {
        return new LaboratorioDto(
                laboratorio.getId(),
                laboratorio.getCoordinacionId(),
                laboratorio.getNombre(),
                laboratorio.getUbicacion(),
                laboratorio.isActivo(),
                bienesUbicados);
    }
}
