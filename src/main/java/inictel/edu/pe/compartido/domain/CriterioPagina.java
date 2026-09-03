package inictel.edu.pe.compartido.domain;

/**
 * Peticion de paginacion y ordenamiento independiente de la tecnologia (RF-21).
 *
 * @param pagina      indice de pagina, comenzando en cero
 * @param tamano      cantidad de registros por pagina
 * @param ordenarPor  nombre del campo por el que se ordena
 * @param descendente true para orden descendente
 */
public record CriterioPagina(int pagina, int tamano, String ordenarPor, boolean descendente) {

    private static final int TAMANO_MAXIMO = 200;

    public CriterioPagina {
        if (pagina < 0) {
            pagina = 0;
        }
        if (tamano <= 0) {
            tamano = 10;
        }
        if (tamano > TAMANO_MAXIMO) {
            tamano = TAMANO_MAXIMO;
        }
    }

    public static CriterioPagina de(int pagina, int tamano, String ordenarPor, boolean descendente) {
        return new CriterioPagina(pagina, tamano, ordenarPor, descendente);
    }
}
