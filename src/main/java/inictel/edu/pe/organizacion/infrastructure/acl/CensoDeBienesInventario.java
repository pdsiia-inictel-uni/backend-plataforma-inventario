package inictel.edu.pe.organizacion.infrastructure.acl;

import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import inictel.edu.pe.organizacion.domain.service.CensoDeBienes;
import inictel.edu.pe.prestamos.application.service.ServicioPublicoPrestamos;
import org.springframework.stereotype.Component;

/**
 * Adaptador anticorrupcion de {@code organizacion} hacia {@code inventario} y
 * {@code prestamos}.
 *
 * <p>Reune en un solo puerto las dos preguntas que la estructura necesita
 * responder antes de desactivar algo: cuantos bienes hay y cuantos siguen
 * fuera (RF-13, RF-14, RNF-39).</p>
 */
@Component
public class CensoDeBienesInventario implements CensoDeBienes {

    private final ServicioPublicoInventario inventario;
    private final ServicioPublicoPrestamos prestamos;

    public CensoDeBienesInventario(ServicioPublicoInventario inventario,
                                   ServicioPublicoPrestamos prestamos) {
        this.inventario = inventario;
        this.prestamos = prestamos;
    }

    @Override
    public long bienesActivosEn(Long coordinacionId) {
        return inventario.contarActivosEnCoordinacion(coordinacionId);
    }

    @Override
    public long bienesUbicadosEn(Long laboratorioId) {
        return inventario.contarEnLaboratorio(laboratorioId);
    }

    @Override
    public long prestamosVigentesEn(Long coordinacionId) {
        return prestamos.contarActivosEn(coordinacionId);
    }

    /** Traduce las cifras del inventario al vocabulario de la estructura. */
    @Override
    public ResumenBienes resumenDe(Long coordinacionId) {
        var resumen = inventario.resumen(coordinacionId);
        return new ResumenBienes(
                resumen.operativos(),
                resumen.prestados(),
                resumen.enMantenimiento(),
                resumen.dadosDeBaja());
    }
}
