package inictel.edu.pe.prestamos.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Raiz del agregado Uso externo (RF-78).
 *
 * <p>Es el prestamo de un equipo para que lo use, dentro de la institucion,
 * personal de otra institucion —la UPC, la UTEC— bajo la responsabilidad de
 * un encargado suyo. Guarda el "Formato de registro de uso de equipos de
 * investigacion" en dos partes:</p>
 * <ul>
 *   <li><b>apertura</b>, puntos 1 a 5: quien responde, quien lo usa, para que
 *       proyecto y hasta cuando. El inicio lo fija el propio agregado;</li>
 *   <li><b>cierre</b>, puntos 6 a 10: en que condiciones se entrego y volvio,
 *       los incidentes y las observaciones.</li>
 * </ul>
 * <p>El punto 9, las firmas, se pone a mano sobre el documento impreso.</p>
 */
public class UsoExterno {

    private static final int MAX_CORTO = 150;
    private static final int MAX_TELEFONO = 30;
    private static final int MAX_LARGO = 1000;

    private Long id;
    private BienPrestado bien;
    private String encargadoNombre;
    private String encargadoCorreo;
    private String encargadoCelular;
    private String estadoEquipoInicio;
    private String usuarioNombre;
    private String usuarioCorreo;
    private String usuarioTelefono;
    private String proyecto;
    private LocalDateTime fechaInicio;
    private LocalDate fechaFinPrevista;
    private LocalTime horaFinPrevista;
    private String actividad;
    private OperadorPrestamo registradoPor;
    private Boolean entregadoOperativo;
    private Boolean devueltoOperativo;
    private String incidente;
    private String accionCorrectiva;
    private String observaciones;
    private LocalDateTime fechaCierre;
    private OperadorPrestamo cerradoPor;
    private EstadoUso estado;

    private UsoExterno() {
    }

    /**
     * Primera parte (puntos 1 a 5). La fecha y la hora de inicio del uso las
     * escribe quien registra, como en el formato en papel; el fin previsto no
     * puede ser anterior al inicio.
     */
    public static UsoExterno abrir(BienPrestado bien,
                                   String estadoEquipoInicio,
                                   String encargadoNombre,
                                   String encargadoCorreo,
                                   String encargadoCelular,
                                   String usuarioNombre,
                                   String usuarioCorreo,
                                   String usuarioTelefono,
                                   String proyecto,
                                   LocalDate fechaInicio,
                                   LocalTime horaInicio,
                                   LocalDate fechaFinPrevista,
                                   LocalTime horaFinPrevista,
                                   String actividad,
                                   OperadorPrestamo registradoPor) {
        if (fechaInicio == null) {
            throw new DatosInvalidosException("fechaInicio", "Indique la fecha de inicio del uso.");
        }
        if (horaInicio == null) {
            throw new DatosInvalidosException("horaInicio", "Indique la hora de inicio del uso.");
        }
        if (fechaFinPrevista != null) {
            boolean antes = fechaFinPrevista.isBefore(fechaInicio)
                    || (fechaFinPrevista.equals(fechaInicio) && horaFinPrevista != null
                            && horaFinPrevista.isBefore(horaInicio));
            if (antes) {
                throw new DatosInvalidosException("fechaFinPrevista",
                        "El fin previsto no puede ser anterior al inicio del uso.");
            }
        }
        UsoExterno uso = new UsoExterno();
        uso.bien = bien;
        uso.estadoEquipoInicio = estadoEquipoInicio;
        uso.encargadoNombre = exigido(encargadoNombre, MAX_CORTO, "encargadoNombre",
                "Indique el investigador encargado.");
        uso.encargadoCorreo = opcional(encargadoCorreo, MAX_CORTO, "encargadoCorreo");
        uso.encargadoCelular = opcional(encargadoCelular, MAX_TELEFONO, "encargadoCelular");
        uso.usuarioNombre = exigido(usuarioNombre, MAX_CORTO, "usuarioNombre",
                "Indique el nombre de quien usará el equipo.");
        uso.usuarioCorreo = opcional(usuarioCorreo, MAX_CORTO, "usuarioCorreo");
        uso.usuarioTelefono = opcional(usuarioTelefono, MAX_TELEFONO, "usuarioTelefono");
        uso.proyecto = opcional(proyecto, MAX_LARGO, "proyecto");
        uso.fechaInicio = LocalDateTime.of(fechaInicio, horaInicio);
        uso.fechaFinPrevista = fechaFinPrevista;
        uso.horaFinPrevista = horaFinPrevista;
        uso.actividad = opcional(actividad, MAX_LARGO, "actividad");
        uso.registradoPor = registradoPor;
        uso.estado = EstadoUso.ABIERTO;
        return uso;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static UsoExterno reconstituir(Long id, BienPrestado bien, String estadoEquipoInicio,
                                          String encargadoNombre,
                                          String encargadoCorreo, String encargadoCelular,
                                          String usuarioNombre, String usuarioCorreo,
                                          String usuarioTelefono, String proyecto,
                                          LocalDateTime fechaInicio, LocalDate fechaFinPrevista,
                                          LocalTime horaFinPrevista, String actividad,
                                          OperadorPrestamo registradoPor,
                                          Boolean entregadoOperativo, Boolean devueltoOperativo,
                                          String incidente, String accionCorrectiva,
                                          String observaciones, LocalDateTime fechaCierre,
                                          OperadorPrestamo cerradoPor, EstadoUso estado) {
        UsoExterno uso = new UsoExterno();
        uso.id = id;
        uso.bien = bien;
        uso.estadoEquipoInicio = estadoEquipoInicio;
        uso.encargadoNombre = encargadoNombre;
        uso.encargadoCorreo = encargadoCorreo;
        uso.encargadoCelular = encargadoCelular;
        uso.usuarioNombre = usuarioNombre;
        uso.usuarioCorreo = usuarioCorreo;
        uso.usuarioTelefono = usuarioTelefono;
        uso.proyecto = proyecto;
        uso.fechaInicio = fechaInicio;
        uso.fechaFinPrevista = fechaFinPrevista;
        uso.horaFinPrevista = horaFinPrevista;
        uso.actividad = actividad;
        uso.registradoPor = registradoPor;
        uso.entregadoOperativo = entregadoOperativo;
        uso.devueltoOperativo = devueltoOperativo;
        uso.incidente = incidente;
        uso.accionCorrectiva = accionCorrectiva;
        uso.observaciones = observaciones;
        uso.fechaCierre = fechaCierre;
        uso.cerradoPor = cerradoPor;
        uso.estado = estado;
        return uso;
    }

    /**
     * Segunda parte (puntos 6 a 10): cierra el uso cuando el equipo vuelve.
     *
     * <p>Las dos preguntas de conformidad son obligatorias: son lo que el cierre
     * viene a dejar escrito. Si el equipo no volvio en condiciones operativas,
     * hay que describir que paso.</p>
     */
    public void cerrar(Boolean entregadoOperativo,
                       Boolean devueltoOperativo,
                       String incidente,
                       String accionCorrectiva,
                       String observaciones,
                       OperadorPrestamo cerradoPor) {
        if (estado == EstadoUso.CERRADO) {
            throw new ReglaNegocioException("Este registro de uso ya está cerrado.");
        }
        DatosInvalidosException errores = null;
        if (entregadoOperativo == null) {
            errores = new DatosInvalidosException("entregadoOperativo",
                    "Indique si el equipo se entregó en condiciones operativas.");
        }
        if (devueltoOperativo == null) {
            errores = errores == null
                    ? new DatosInvalidosException("devueltoOperativo",
                            "Indique si el equipo se devolvió en condiciones operativas.")
                    : errores.agregar("devueltoOperativo",
                            "Indique si el equipo se devolvió en condiciones operativas.");
        }
        if (Boolean.FALSE.equals(devueltoOperativo) && (incidente == null || incidente.isBlank())) {
            errores = errores == null
                    ? new DatosInvalidosException("incidente",
                            "Si el equipo no volvió en condiciones operativas, describa el incidente o la falla.")
                    : errores.agregar("incidente",
                            "Si el equipo no volvió en condiciones operativas, describa el incidente o la falla.");
        }
        if (errores != null) {
            throw errores;
        }
        this.entregadoOperativo = entregadoOperativo;
        this.devueltoOperativo = devueltoOperativo;
        this.incidente = opcional(incidente, MAX_LARGO, "incidente");
        this.accionCorrectiva = opcional(accionCorrectiva, MAX_LARGO, "accionCorrectiva");
        this.observaciones = opcional(observaciones, MAX_LARGO, "observaciones");
        this.fechaCierre = LocalDateTime.now();
        this.cerradoPor = cerradoPor;
        this.estado = EstadoUso.CERRADO;
    }

    /**
     * Solo se anula un uso que tiene su primera parte y no la final: se
     * registro la salida, pero se cancelo antes de usarse el equipo.
     */
    public void exigirAnulable() {
        if (estado != EstadoUso.ABIERTO) {
            throw new ReglaNegocioException(
                    "Este registro de uso ya está cerrado y forma parte de la historia del equipo: no se puede anular.");
        }
    }

    /** El equipo volvio en malas condiciones: queda con revision pendiente (RN-19). */
    public boolean reportaDano() {
        return Boolean.FALSE.equals(devueltoOperativo);
    }

    public boolean estaAbierto() {
        return estado == EstadoUso.ABIERTO;
    }

    public Long coordinacionId() {
        return bien == null ? null : bien.coordinacionId();
    }

    private static String exigido(String valor, int maximo, String campo, String mensaje) {
        String limpio = opcional(valor, maximo, campo);
        if (limpio == null) {
            throw new DatosInvalidosException(campo, mensaje);
        }
        return limpio;
    }

    private static String opcional(String valor, int maximo, String campo) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > maximo) {
            throw new DatosInvalidosException(campo,
                    "El texto no puede superar los " + maximo + " caracteres.");
        }
        return limpio;
    }

    // ------------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------------

    public Long getId() { return id; }

    public void asignarId(Long id) { this.id = id; }

    public BienPrestado getBien() { return bien; }

    public String getEncargadoNombre() { return encargadoNombre; }

    public String getEncargadoCorreo() { return encargadoCorreo; }

    public String getEncargadoCelular() { return encargadoCelular; }

    public String getEstadoEquipoInicio() { return estadoEquipoInicio; }

    public String getUsuarioNombre() { return usuarioNombre; }

    public String getUsuarioCorreo() { return usuarioCorreo; }

    public String getUsuarioTelefono() { return usuarioTelefono; }

    public String getProyecto() { return proyecto; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }

    public LocalDate getFechaFinPrevista() { return fechaFinPrevista; }

    public LocalTime getHoraFinPrevista() { return horaFinPrevista; }

    public String getActividad() { return actividad; }

    public OperadorPrestamo getRegistradoPor() { return registradoPor; }

    public Boolean getEntregadoOperativo() { return entregadoOperativo; }

    public Boolean getDevueltoOperativo() { return devueltoOperativo; }

    public String getIncidente() { return incidente; }

    public String getAccionCorrectiva() { return accionCorrectiva; }

    public String getObservaciones() { return observaciones; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }

    public OperadorPrestamo getCerradoPor() { return cerradoPor; }

    public EstadoUso getEstado() { return estado; }
}
