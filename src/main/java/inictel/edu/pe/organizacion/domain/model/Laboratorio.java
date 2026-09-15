package inictel.edu.pe.organizacion.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

/**
 * Raiz del agregado Laboratorio (RF-12).
 *
 * <p>Es la ubicacion fisica opcional de un bien. Pertenece siempre a una
 * Coordinacion (RN-03); un bien nunca puede ubicarse en un laboratorio de otra
 * Coordinacion (RN-12). No tiene estado: un laboratorio no se desactiva.</p>
 */
public class Laboratorio {

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_UBICACION = 200;

    private Long id;
    private Long coordinacionId;
    private String nombre;
    private String ubicacion;

    private Laboratorio() {
    }

    public static Laboratorio crear(Long coordinacionId, String nombre, String ubicacion) {
        if (coordinacionId == null) {
            throw new DatosInvalidosException("coordinacionId", "Seleccione la coordinación a la que pertenece.");
        }
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.coordinacionId = coordinacionId;
        laboratorio.nombre = exigirNombre(nombre);
        laboratorio.ubicacion = normalizar(ubicacion);
        return laboratorio;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Laboratorio reconstituir(Long id, Long coordinacionId, String nombre, String ubicacion) {
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.id = id;
        laboratorio.coordinacionId = coordinacionId;
        laboratorio.nombre = nombre;
        laboratorio.ubicacion = ubicacion;
        return laboratorio;
    }

    public void actualizar(String nombre, String ubicacion) {
        this.nombre = exigirNombre(nombre);
        this.ubicacion = normalizar(ubicacion);
    }

    public boolean perteneceA(Long coordinacion) {
        return coordinacionId != null && coordinacionId.equals(coordinacion);
    }

    private static String exigirNombre(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException("nombre", "Ingrese el nombre del laboratorio.");
        }
        if (limpio.length() > MAX_NOMBRE) {
            throw new DatosInvalidosException("nombre", "El nombre no puede superar los 150 caracteres.");
        }
        return limpio;
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > MAX_UBICACION) {
            throw new DatosInvalidosException("ubicacion", "La ubicación excede la longitud permitida.");
        }
        return limpio;
    }

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public Long getCoordinacionId() {
        return coordinacionId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }
}
