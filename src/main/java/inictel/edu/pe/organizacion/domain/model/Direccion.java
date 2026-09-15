package inictel.edu.pe.organizacion.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

/**
 * Raiz del agregado Direccion (RF-10). Primer nivel de la jerarquia bajo
 * {@link Institucion}.
 *
 * <p>Las direcciones de INICTEL-UNI son las dos de su reglamento y no cambian
 * con el uso del sistema: se precargan con el esquema (migracion V2) y la
 * aplicacion no las crea ni las desactiva. Por eso el agregado solo ofrece
 * reconstituirse y corregir su nombre y su sigla; no tiene estado ni
 * transiciones que nadie pueda invocar.</p>
 */
public class Direccion {

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_SIGLA = 20;

    private Long id;
    private String nombre;
    private String sigla;

    private Direccion() {
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Direccion reconstituir(Long id, String nombre, String sigla) {
        Direccion direccion = new Direccion();
        direccion.id = id;
        direccion.nombre = nombre;
        direccion.sigla = sigla;
        return direccion;
    }

    public void actualizar(String nombre, String sigla) {
        this.nombre = exigirNombre(nombre);
        this.sigla = normalizarSigla(sigla);
    }

    private static String exigirNombre(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException("nombre", "Ingrese el nombre de la dirección.");
        }
        if (limpio.length() > MAX_NOMBRE) {
            throw new DatosInvalidosException("nombre", "El nombre no puede superar los 150 caracteres.");
        }
        return limpio;
    }

    private static String normalizarSigla(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > MAX_SIGLA) {
            throw new DatosInvalidosException("sigla", "El texto excede la longitud permitida.");
        }
        return limpio;
    }

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSigla() {
        return sigla;
    }
}
