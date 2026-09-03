package inictel.edu.pe.organizacion.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.time.LocalDateTime;

/**
 * Raiz del agregado Direccion (RF-10). Primer nivel de la jerarquia bajo
 * {@link Institucion}.
 *
 * <p>Las direcciones de INICTEL-UNI son las dos de su reglamento y no cambian
 * con el uso del sistema: se precargan con el esquema (migracion V4) y la
 * aplicacion no las crea ni las desactiva. Por eso el agregado solo ofrece
 * reconstituirse y corregir sus datos; no hay fabrica de alta ni transiciones
 * de estado que nadie pueda invocar.</p>
 */
public class Direccion {

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_SIGLA = 20;
    private static final int MAX_DESCRIPCION = 500;

    private Long id;
    private String nombre;
    private String sigla;
    private String descripcion;
    private boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Direccion() {
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Direccion reconstituir(Long id, String nombre, String sigla, String descripcion,
                                         boolean activa, LocalDateTime fechaCreacion,
                                         LocalDateTime fechaActualizacion) {
        Direccion direccion = new Direccion();
        direccion.id = id;
        direccion.nombre = nombre;
        direccion.sigla = sigla;
        direccion.descripcion = descripcion;
        direccion.activa = activa;
        direccion.fechaCreacion = fechaCreacion;
        direccion.fechaActualizacion = fechaActualizacion;
        return direccion;
    }

    public void actualizar(String nombre, String sigla, String descripcion) {
        this.nombre = exigirNombre(nombre);
        this.sigla = normalizar(sigla, MAX_SIGLA, "sigla");
        this.descripcion = normalizar(descripcion, MAX_DESCRIPCION, "descripcion");
        this.fechaActualizacion = LocalDateTime.now();
    }

    private static String exigirNombre(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException("nombre", "Ingrese el nombre de la direccion.");
        }
        if (limpio.length() > MAX_NOMBRE) {
            throw new DatosInvalidosException("nombre", "El nombre no puede superar los 150 caracteres.");
        }
        return limpio;
    }

    private static String normalizar(String valor, int maximo, String campo) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > maximo) {
            throw new DatosInvalidosException(campo, "El texto excede la longitud permitida.");
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

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActiva() {
        return activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
