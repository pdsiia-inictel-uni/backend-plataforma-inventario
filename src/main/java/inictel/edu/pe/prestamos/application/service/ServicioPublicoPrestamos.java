package inictel.edu.pe.prestamos.application.service;

import inictel.edu.pe.prestamos.domain.repository.PrestamoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de host abierto del contexto prestamos: cifras que consumen el panel
 * de control (RF-75 .. RF-77), sin
 * exponer el agregado.
 *
 * <p>Con {@code coordinacionId} nulo las cifras abarcan toda la institucion.</p>
 */
@Service
public class ServicioPublicoPrestamos {

    private final PrestamoRepositorio prestamos;

    public ServicioPublicoPrestamos(PrestamoRepositorio prestamos) {
        this.prestamos = prestamos;
    }

    @Transactional(readOnly = true)
    public long contarActivos(Long coordinacionId) {
        return prestamos.contarActivos(coordinacionId);
    }

    @Transactional(readOnly = true)
    public long contarVencidos(Long coordinacionId) {
        return prestamos.contarVencidos(coordinacionId);
    }
}
