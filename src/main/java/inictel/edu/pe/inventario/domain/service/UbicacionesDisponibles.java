package inictel.edu.pe.inventario.domain.service;

import java.util.Optional;

/**
 * Puerto hacia {@code organizacion} para validar y describir la ubicacion
 * fisica de un bien (RN-12, RNF-39).
 */
public interface UbicacionesDisponibles {

    /**
     * RN-12: el laboratorio existe, esta activo y pertenece a la Coordinacion
     * indicada. Impide ubicar un bien en un laboratorio ajeno.
     */
    boolean laboratorioPerteneceA(Long laboratorioId, Long coordinacionId);

    /**
     * RN-26: la Coordinacion tiene al menos un laboratorio activo donde ubicar
     * sus bienes. Sin laboratorios no se registra ningun bien.
     */
    boolean tieneLaboratoriosActivos(Long coordinacionId);

    Optional<String> nombreDeLaboratorio(Long laboratorioId);

    Optional<String> nombreDeCoordinacion(Long coordinacionId);
}
