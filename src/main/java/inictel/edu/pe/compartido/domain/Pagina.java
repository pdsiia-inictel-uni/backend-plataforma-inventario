package inictel.edu.pe.compartido.domain;

import java.util.List;
import java.util.function.Function;

/**
 * Resultado paginado expresado en terminos del dominio.
 *
 * <p>El dominio y la capa de aplicacion no dependen de las abstracciones de
 * Spring Data; la traduccion desde {@code Page} ocurre en infraestructura.</p>
 */
public record Pagina<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas) {

    public boolean primera() {
        return pagina == 0;
    }

    public boolean ultima() {
        return totalPaginas == 0 || pagina >= totalPaginas - 1;
    }

    /** Convierte el contenido conservando los datos de paginacion. */
    public <R> Pagina<R> mapear(Function<T, R> conversor) {
        return new Pagina<>(contenido.stream().map(conversor).toList(), pagina, tamano, totalElementos, totalPaginas);
    }

    public static <T> Pagina<T> vacia(int tamano) {
        return new Pagina<>(List.of(), 0, tamano, 0, 0);
    }
}
