package inictel.edu.pe.organizacion.infrastructure.acl;

import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import inictel.edu.pe.organizacion.domain.service.CensoDeBienes;
import org.springframework.stereotype.Component;

/**
 * Adaptador anticorrupcion de {@code organizacion} hacia {@code inventario}.
 *
 * <p>Traduce las cifras del inventario al vocabulario de la estructura: cuantos
 * bienes tiene una coordinacion y en que condicion estan los de cada
 * laboratorio (RF-14, RF-15, RNF-39).</p>
 */
@Component
public class CensoDeBienesInventario implements CensoDeBienes {

    private final ServicioPublicoInventario inventario;

    public CensoDeBienesInventario(ServicioPublicoInventario inventario) {
        this.inventario = inventario;
    }

    @Override
    public ResumenBienes resumenDe(Long coordinacionId) {
        var resumen = inventario.resumen(coordinacionId);
        return new ResumenBienes(
                resumen.operativos(),
                resumen.prestados(),
                resumen.enMantenimiento(),
                resumen.dadosDeBaja());
    }

    @Override
    public ResumenBienes resumenDeLaboratorio(Long laboratorioId) {
        var conteo = inventario.bienesEnLaboratorio(laboratorioId);
        return new ResumenBienes(
                conteo.operativos(),
                conteo.prestados(),
                conteo.enMantenimiento(),
                conteo.dadosDeBaja());
    }
}
