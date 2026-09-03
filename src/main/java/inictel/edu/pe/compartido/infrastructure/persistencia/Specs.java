package inictel.edu.pe.compartido.infrastructure.persistencia;

import org.springframework.data.jpa.domain.Specification;

/**
 * Utilitario para componer specifications ignorando los filtros no informados.
 */
public final class Specs {

    private Specs() {
    }

    @SafeVarargs
    public static <T> Specification<T> combinar(Specification<T>... specs) {
        Specification<T> resultado = null;
        for (Specification<T> spec : specs) {
            if (spec == null) {
                continue;
            }
            resultado = (resultado == null) ? spec : resultado.and(spec);
        }
        return resultado;
    }
}
