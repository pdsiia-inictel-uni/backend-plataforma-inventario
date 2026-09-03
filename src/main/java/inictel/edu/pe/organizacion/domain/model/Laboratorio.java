package inictel.edu.pe.organizacion.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;

import java.time.LocalDateTime;

/**
 * Raiz del agregado Laboratorio (RF-12).
 *
 * <p>Es la ubicacion fisica opcional de un bien. Pertenece siempre a una
 * Coordinacion (RN-03); un bien nunca puede ubicarse en un laboratorio de otra
 * Coordinacion (RN-12).</p>
 */
public class Laboratorio {

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_UBICACION = 200;

    private Long id;
    private Long coordinacionId;
    private String nombre;
    private String ubicacion;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private Laboratorio() {
    }

    public static Laboratorio crear(Long coordinacionId, String nombre, String ubicacion) {
        if (coordinacionId == null) {
            throw new DatosInvalidosException("coordinacionId", "Seleccione la coordinacion a la que pertenece.");
        }
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.coordinacionId = coordinacionId;
        laboratorio.nombre = exigirNombre(nombre);
        laboratorio.ubicacion = normalizar(ubicacion);
        laboratorio.activo = true;
        laboratorio.fechaCreacion = LocalDateTime.now();
        return laboratorio;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Laboratorio reconstituir(Long id, Long coordinacionId, String nombre, String ubicacion,
                                           boolean activo, LocalDateTime fechaCreacion,
                                           LocalDateTime fechaActualizacion) {
        Laboratorio laboratorio = new Laboratorio();
        laboratorio.id = id;
        laboratorio.coordinacionId = coordinacionId;
        laboratorio.nombre = nombre;
        laboratorio.ubicacion = ubicacion;
        laboratorio.activo = activo;
        laboratorio.fechaCreacion = fechaCreacion;
        laboratorio.fechaActualizacion = fechaActualizacion;
        return laboratorio;
    }

    public void actualizar(String nombre, String ubicacion) {
        this.nombre = exigirNombre(nombre);
        this.ubicacion = normalizar(ubicacion);
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-14, RN-26: primero hay que reubicar los bienes que estan dentro, y
     * nunca se desactiva el ultimo laboratorio activo de la Coordinacion.
     *
     * <p>Sin laboratorios no pueden registrarse bienes (RN-26): una
     * Coordinacion que se queda sin ninguno deja de poder trabajar, asi que la
     * regla se aplica aqui y no en la pantalla.</p>
     */
    public void desactivar(long bienesUbicados, boolean esElUltimoActivo) {
        if (!activo) {
            throw new ReglaNegocioException("El laboratorio ya se encuentra desactivado.");
        }
        if (esElUltimoActivo) {
            throw new ReglaNegocioException(
                    "No se puede desactivar el unico laboratorio de la coordinacion. "
                            + "Cree otro laboratorio antes de desactivar este.");
        }
        if (bienesUbicados > 0) {
            throw new ReglaNegocioException(
                    "No se puede desactivar el laboratorio porque tiene " + bienesUbicados
                            + " bien(es) ubicado(s) en el. Reubiquelos primero.");
        }
        this.activo = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void activar() {
        if (activo) {
            throw new ReglaNegocioException("El laboratorio ya se encuentra activo.");
        }
        this.activo = true;
        this.fechaActualizacion = LocalDateTime.now();
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
            throw new DatosInvalidosException("ubicacion", "La ubicacion excede la longitud permitida.");
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

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
