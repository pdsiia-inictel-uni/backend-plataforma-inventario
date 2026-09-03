package inictel.edu.pe.inventario.domain.repository;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.Equipo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Puerto de persistencia del agregado Bien. */
public interface EquipoRepositorio {

    Equipo guardar(Equipo equipo);

    Optional<Equipo> buscarPorId(Long id);

    Pagina<Equipo> buscar(FiltroEquipos filtro, CriterioPagina criterio);

    /** Listado completo para exportaciones (RF-52); ordenado por nombre. */
    List<Equipo> listar(FiltroEquipos filtro);

    /** RN-14: unicidad institucional, sin distincion de Coordinacion. */
    boolean existeNumeroSerie(String numeroSerie, Long idExcluido);

    boolean existeCodigoInventario(String codigo, Long idExcluido);

    boolean existeCodigoPatrimonial(String codigo, Long idExcluido);

    long contarActivosEnCategoria(Long categoriaId);

    // ------------------------------------------------------------------
    // RF-83, RN-38: bienes a cargo de una persona
    // ------------------------------------------------------------------

    /**
     * Bienes <b>en servicio</b> que esa persona tiene a su nombre.
     *
     * <p>Los dados de baja no cuentan: al darlos de baja dejan de estar a
     * cargo de nadie, porque nadie responde por el uso de algo que ya no se
     * usa. Si contaran, un Operador quedaria atado para siempre a un equipo
     * que ya no existe y no podria dejar su puesto nunca (RN-37).</p>
     */
    long contarACargoDe(Long usuarioId);

    /** Los bienes en servicio a nombre de esa persona, para reasignarlos. */
    List<Equipo> listarACargoDe(Long usuarioId);

    // ------------------------------------------------------------------
    // Consultas de apoyo a organizacion (RF-13, RF-14, RF-15)
    // ------------------------------------------------------------------

    long contarActivosEnCoordinacion(Long coordinacionId);

    long contarEnLaboratorio(Long laboratorioId);

    // ------------------------------------------------------------------
    // Consultas para el panel de control (RF-75 .. RF-77)
    // ------------------------------------------------------------------

    /**
     * Bienes por condicion. Con {@code coordinacionId} nulo abarca toda la
     * institucion, que es la vista del Administrador.
     */
    Map<CondicionEquipo, Long> contarPorCondicion(Long coordinacionId);

    /** Bienes activos por nombre de categoria. */
    Map<String, Long> contarPorCategoria(Long coordinacionId);

    /** RF-76: bienes devueltos con dano a la espera de decision del Responsable. */
    long contarRevisionPendiente(Long coordinacionId);
}
