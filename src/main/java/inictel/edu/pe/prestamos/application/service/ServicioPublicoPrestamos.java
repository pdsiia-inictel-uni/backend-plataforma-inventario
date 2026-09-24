package inictel.edu.pe.prestamos.application.service;

import inictel.edu.pe.prestamos.application.dto.UsoExternoDto;
import inictel.edu.pe.prestamos.domain.repository.PrestamoRepositorio;
import inictel.edu.pe.prestamos.domain.repository.UsoExternoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    private final UsoExternoRepositorio usos;

    public ServicioPublicoPrestamos(PrestamoRepositorio prestamos, UsoExternoRepositorio usos) {
        this.prestamos = prestamos;
        this.usos = usos;
    }

    /**
     * RF-78: un registro de uso externo, para imprimir su formato. Quien lo
     * consume comprueba el acceso a la coordinacion del equipo.
     */
    @Transactional(readOnly = true)
    public Optional<UsoExternoDto> usoExterno(Long id) {
        return id == null ? Optional.empty() : usos.buscarPorId(id).map(UsoExternoDto::de);
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
