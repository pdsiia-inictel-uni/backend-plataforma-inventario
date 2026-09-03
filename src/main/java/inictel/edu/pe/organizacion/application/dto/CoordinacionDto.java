package inictel.edu.pe.organizacion.application.dto;

import inictel.edu.pe.organizacion.domain.model.Coordinacion;
import inictel.edu.pe.organizacion.domain.service.CensoDeBienes;
import inictel.edu.pe.organizacion.domain.service.CensoDePersonal;

/**
 * Coordinacion con el resumen que la vista de estructura necesita mostrar en
 * linea (RF-15), para que el Administrador vea el estado de cada una sin tener
 * que entrar a ella.
 *
 * @param responsable    nombre del Responsable vigente; {@code null} si no hay
 *                       (alerta critica de la vista)
 */
public record CoordinacionDto(
        Long id,
        Long direccionId,
        String direccionNombre,
        String nombre,
        String descripcion,
        boolean activa,
        Long responsableId,
        String responsable,
        long operadores,
        long laboratorios,
        long bienesOperativos,
        long bienesPrestados,
        long bienesEnMantenimiento,
        long bienesDadosDeBaja,
        long bienesTotal) {

    public static CoordinacionDto de(Coordinacion coordinacion,
                                     String direccionNombre,
                                     CensoDePersonal.ResponsableVigente responsable,
                                     long operadores,
                                     long laboratorios,
                                     CensoDeBienes.ResumenBienes bienes) {
        return new CoordinacionDto(
                coordinacion.getId(),
                coordinacion.getDireccionId(),
                direccionNombre,
                coordinacion.getNombre(),
                coordinacion.getDescripcion(),
                coordinacion.isActiva(),
                responsable == null ? null : responsable.id(),
                responsable == null ? null : responsable.nombreCompleto(),
                operadores,
                laboratorios,
                bienes.operativos(),
                bienes.prestados(),
                bienes.enMantenimiento(),
                bienes.dadosDeBaja(),
                bienes.total());
    }

    /** RF-15: la ausencia de Responsable es la alerta principal de la vista. */
    public boolean sinResponsable() {
        return responsableId == null;
    }
}
