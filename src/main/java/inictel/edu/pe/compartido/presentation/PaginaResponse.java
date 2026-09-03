package inictel.edu.pe.compartido.presentation;

import inictel.edu.pe.compartido.domain.Pagina;

import java.util.List;

/**
 * Envoltura de paginacion devuelta por la API (RF-21).
 *
 * <p>Es la representacion HTTP de {@link Pagina}; el contrato con el frontend
 * se mantiene identico al de la version anterior del sistema.</p>
 */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean primera,
        boolean ultima) {

    public static <T> PaginaResponse<T> de(Pagina<T> pagina) {
        return new PaginaResponse<>(
                pagina.contenido(),
                pagina.pagina(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas(),
                pagina.primera(),
                pagina.ultima());
    }
}
