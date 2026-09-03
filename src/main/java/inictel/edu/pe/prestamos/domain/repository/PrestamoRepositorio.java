package inictel.edu.pe.prestamos.domain.repository;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.prestamos.domain.model.Prestamo;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia del agregado Prestamo. */
public interface PrestamoRepositorio {

    Prestamo guardar(Prestamo prestamo);

    Optional<Prestamo> buscarPorId(Long id);

    Pagina<Prestamo> buscar(FiltroPrestamos filtro, CriterioPagina criterio);

    /** RF-66: historial completo de un bien. */
    List<Prestamo> historialPorBien(Long equipoId);

    /** RF-66: historial de una persona dentro de una Coordinacion. */
    List<Prestamo> historialPorPersona(String dni, Long coordinacionId);

    /** RF-67: prestamos activos que superaron la fecha estimada de devolucion. */
    List<Prestamo> vencidos(Long coordinacionId);

    /** RN-16: un bien con prestamo activo no puede volver a prestarse. */
    boolean tienePrestamoActivo(Long equipoId);

    /** Con {@code coordinacionId} nulo abarca toda la institucion. */
    long contarActivos(Long coordinacionId);

    long contarVencidos(Long coordinacionId);

    /** RF-77: salidas registradas por una persona, para su panel de inicio. */
    long contarRegistradosPor(Long usuarioId, Long coordinacionId);
}
