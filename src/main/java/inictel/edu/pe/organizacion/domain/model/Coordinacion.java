package inictel.edu.pe.organizacion.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;

import java.time.LocalDateTime;

/**
 * Raiz del agregado Coordinacion (RF-11).
 *
 * <p>Es la unidad de aislamiento del sistema (RN-23): posee su propio
 * inventario, su unico Responsable y sus Operadores. Todo el modelo de
 * permisos gira alrededor de esta entidad.
 *
 * <p>No tiene sigla: en la institucion las coordinaciones se nombran por su
 * nombre, y el campo se quedaba vacio o se rellenaba con una abreviatura
 * inventada en el momento (v3.5).</p></p>
 */
public class Coordinacion {

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_DESCRIPCION = 500;

    private Long id;
    private Long direccionId;
    private String nombre;
    private String descripcion;
    private boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Coordinacion() {
    }

    public static Coordinacion crear(Long direccionId, String nombre, String descripcion) {
        if (direccionId == null) {
            throw new DatosInvalidosException("direccionId", "Seleccione la direccion a la que pertenece.");
        }
        Coordinacion coordinacion = new Coordinacion();
        coordinacion.direccionId = direccionId;
        coordinacion.nombre = exigirNombre(nombre);
        coordinacion.descripcion = normalizar(descripcion, MAX_DESCRIPCION, "descripcion");
        coordinacion.activa = true;
        coordinacion.fechaCreacion = LocalDateTime.now();
        return coordinacion;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Coordinacion reconstituir(Long id, Long direccionId, String nombre,
                                            String descripcion, boolean activa, LocalDateTime fechaCreacion,
                                            LocalDateTime fechaActualizacion) {
        Coordinacion coordinacion = new Coordinacion();
        coordinacion.id = id;
        coordinacion.direccionId = direccionId;
        coordinacion.nombre = nombre;
        coordinacion.descripcion = descripcion;
        coordinacion.activa = activa;
        coordinacion.fechaCreacion = fechaCreacion;
        coordinacion.fechaActualizacion = fechaActualizacion;
        return coordinacion;
    }

    public void actualizar(String nombre, String descripcion) {
        this.nombre = exigirNombre(nombre);
        this.descripcion = normalizar(descripcion, MAX_DESCRIPCION, "descripcion");
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-13: no se desactiva una Coordinacion con bienes activos ni con
     * prestamos vigentes. El mensaje explica el motivo concreto para que el
     * Administrador sepa que hacer (RNF-25).
     */
    public void desactivar(long bienesActivos, long prestamosVigentes) {
        if (!activa) {
            throw new ReglaNegocioException("La coordinacion ya se encuentra desactivada.");
        }
        if (prestamosVigentes > 0) {
            throw new ReglaNegocioException(
                    "No se puede desactivar la coordinacion porque tiene " + prestamosVigentes
                            + " prestamo(s) sin devolver. Registre las devoluciones primero.");
        }
        if (bienesActivos > 0) {
            throw new ReglaNegocioException(
                    "No se puede desactivar la coordinacion porque tiene " + bienesActivos
                            + " bien(es) activo(s) en su inventario.");
        }
        this.activa = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void activar() {
        if (activa) {
            throw new ReglaNegocioException("La coordinacion ya se encuentra activa.");
        }
        this.activa = true;
        this.fechaActualizacion = LocalDateTime.now();
    }

    private static String exigirNombre(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException("nombre", "Ingrese el nombre de la coordinacion.");
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

    public Long getDireccionId() {
        return direccionId;
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

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
