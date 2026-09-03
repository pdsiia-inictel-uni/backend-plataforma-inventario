package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;

import java.time.LocalDateTime;

/**
 * Raiz del agregado Categoria de bienes (RF-31 .. RF-33).
 *
 * <p>Catalogo institucional compartido por todas las Coordinaciones: clasificar
 * un dron como dron no depende de quien lo custodia. Solo el Administrador lo
 * mantiene.</p>
 *
 * <p>La plantilla de atributos sugeridos de la version 1.0 desaparece junto con
 * el modelo EAV: el bien pasa a tener campos fijos.</p>
 */
public class Categoria {

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_DESCRIPCION = 500;

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean activa;
    private LocalDateTime fechaCreacion;

    private Categoria() {
    }

    public static Categoria crear(String nombre, String descripcion, boolean activa) {
        Categoria categoria = new Categoria();
        categoria.nombre = exigirNombre(nombre);
        categoria.descripcion = normalizarDescripcion(descripcion);
        categoria.activa = activa;
        categoria.fechaCreacion = LocalDateTime.now();
        return categoria;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Categoria reconstituir(Long id, String nombre, String descripcion, boolean activa,
                                         LocalDateTime fechaCreacion) {
        Categoria categoria = new Categoria();
        categoria.id = id;
        categoria.nombre = nombre;
        categoria.descripcion = descripcion;
        categoria.activa = activa;
        categoria.fechaCreacion = fechaCreacion;
        return categoria;
    }

    public void actualizarDatos(String nombre, String descripcion) {
        this.nombre = exigirNombre(nombre);
        this.descripcion = normalizarDescripcion(descripcion);
    }

    public void activar() {
        this.activa = true;
    }

    /** RF-33: no se desactiva una categoria que todavia clasifica bienes activos. */
    public void desactivar(long bienesActivos) {
        if (bienesActivos > 0) {
            throw new ReglaNegocioException(
                    "No se puede desactivar la categoria porque " + bienesActivos
                            + " bien(es) activo(s) la utilizan.");
        }
        this.activa = false;
    }

    public ReferenciaCategoria referencia() {
        return new ReferenciaCategoria(id, nombre);
    }

    private static String exigirNombre(String nombre) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException("nombre", "Ingrese el nombre de la categoria.");
        }
        if (limpio.length() > MAX_NOMBRE) {
            throw new DatosInvalidosException("nombre", "El nombre no puede superar los 100 caracteres.");
        }
        return limpio;
    }

    private static String normalizarDescripcion(String descripcion) {
        if (descripcion == null) {
            return null;
        }
        String limpio = descripcion.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > MAX_DESCRIPCION) {
            throw new DatosInvalidosException("descripcion",
                    "La descripcion no puede superar los 500 caracteres.");
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

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActiva() {
        return activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
