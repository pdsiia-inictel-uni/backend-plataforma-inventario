package inictel.edu.pe.compartido.infrastructure.persistencia;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

/**
 * Traduce entre el vocabulario de paginacion del dominio y el de Spring Data.
 *
 * <p>Vive en infraestructura precisamente para que {@code CriterioPagina} y
 * {@code Pagina} del dominio no arrastren dependencias del framework.</p>
 */
public final class PaginacionJpa {

    private PaginacionJpa() {
    }

    public static Pageable aPageable(CriterioPagina criterio, String ordenPorDefecto) {
        String campo = (criterio.ordenarPor() == null || criterio.ordenarPor().isBlank())
                ? ordenPorDefecto
                : criterio.ordenarPor();
        Sort orden = Sort.by(criterio.descendente() ? Sort.Direction.DESC : Sort.Direction.ASC, campo);
        return PageRequest.of(criterio.pagina(), criterio.tamano(), orden);
    }

    public static <E, T> Pagina<T> aPagina(Page<E> page, Function<E, T> conversor) {
        return new Pagina<>(
                page.getContent().stream().map(conversor).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
