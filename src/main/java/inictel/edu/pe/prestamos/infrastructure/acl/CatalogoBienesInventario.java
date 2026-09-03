package inictel.edu.pe.prestamos.infrastructure.acl;

import inictel.edu.pe.inventario.application.dto.BienPrestableDto;
import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import inictel.edu.pe.prestamos.domain.model.BienPrestado;
import inictel.edu.pe.prestamos.domain.service.CatalogoBienes;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Capa anticorrupcion hacia {@code inventario}: traduce el vocabulario de
 * prestamos ("el bien esta disponible", "marcar prestado") a las operaciones
 * que publica aquel contexto (RNF-39).
 */
@Component
public class CatalogoBienesInventario implements CatalogoBienes {

    private final ServicioPublicoInventario inventario;

    public CatalogoBienesInventario(ServicioPublicoInventario inventario) {
        this.inventario = inventario;
    }

    @Override
    public Optional<BienPrestado> buscar(Long equipoId) {
        return inventario.buscarBien(equipoId)
                .map(b -> new BienPrestado(b.id(), b.coordinacionId(), b.nombre(),
                        b.codigoInventario(), b.numeroSerie()));
    }

    @Override
    public boolean estaDisponible(Long equipoId) {
        return inventario.buscarBien(equipoId).map(BienPrestableDto::disponible).orElse(false);
    }

    @Override
    public String condicionActual(Long equipoId) {
        return inventario.buscarBien(equipoId).map(BienPrestableDto::condicionEtiqueta).orElse("desconocida");
    }

    @Override
    public boolean estaDeBaja(Long equipoId) {
        return inventario.buscarBien(equipoId).map(b -> !b.activo()).orElse(false);
    }

    @Override
    public void marcarPrestado(Long equipoId, String detalle) {
        inventario.marcarPrestado(equipoId, detalle);
    }

    @Override
    public void registrarRetorno(Long equipoId, boolean reportaDano, String detalle) {
        inventario.registrarRetorno(equipoId, reportaDano, detalle);
    }
}
