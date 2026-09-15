package inictel.edu.pe.organizacion.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

/**
 * Raiz del agregado Coordinacion (RF-11).
 *
 * <p>Es la unidad de aislamiento del sistema (RN-23): posee su propio
 * inventario, su unico Responsable y sus Operadores. Todo el modelo de
 * permisos gira alrededor de esta entidad.</p>
 *
 * <p>No tiene sigla: en la institucion las coordinaciones se nombran por su
 * nombre, y el campo se quedaba vacio o se rellenaba con una abreviatura
 * inventada en el momento (v3.5). Tampoco tiene estado: es una unidad estable
 * de la institucion, como las Direcciones, y no se desactiva (RF-13).</p>
 */
public class Coordinacion {

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_DESCRIPCION = 500;

    private Long id;
    private Long direccionId;
    private String nombre;
    private String descripcion;

    private Coordinacion() {
    }

    public static Coordinacion crear(Long direccionId, String nombre, String descripcion) {
        if (direccionId == null) {
            throw new DatosInvalidosException("direccionId", "Seleccione la dirección a la que pertenece.");
        }
        Coordinacion coordinacion = new Coordinacion();
        coordinacion.direccionId = direccionId;
        coordinacion.nombre = exigirNombre(nombre);
        coordinacion.descripcion = normalizar(descripcion, MAX_DESCRIPCION, "descripcion");
        return coordinacion;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Coordinacion reconstituir(Long id, Long direccionId, String nombre, String descripcion) {
        Coordinacion coordinacion = new Coordinacion();
        coordinacion.id = id;
        coordinacion.direccionId = direccionId;
        coordinacion.nombre = nombre;
        coordinacion.descripcion = descripcion;
        return coordinacion;
    }

    public void actualizar(String nombre, String descripcion) {
        this.nombre = exigirNombre(nombre);
        this.descripcion = normalizar(descripcion, MAX_DESCRIPCION, "descripcion");
    }

    private static String exigirNombre(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException("nombre", "Ingrese el nombre de la coordinación.");
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
}
