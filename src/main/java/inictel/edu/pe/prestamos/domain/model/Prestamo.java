package inictel.edu.pe.prestamos.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Raiz del agregado Prestamo (RF-58 .. RF-68).
 *
 * <p>Las fechas de salida y de retorno las fija el propio agregado, nunca el
 * cliente (RN-21).</p>
 */
public class Prestamo {

    private static final int MAX_DESTINO = 200;
    private static final int MAX_OBSERVACIONES = 1000;

    private Long id;
    private BienPrestado bien;
    private PersonaResponsable persona;
    private String destino;
    private LocalDateTime fechaPrestamo;
    private LocalDate fechaEstimadaDevolucion;
    private String observacionesSalida;
    private LocalDateTime fechaDevolucion;
    private Boolean conforme;
    private boolean reportaDano;
    private String observacionesRetorno;
    private OperadorPrestamo usuarioPresta;
    private OperadorPrestamo usuarioRecibe;
    private EstadoPrestamo estado;

    private Prestamo() {
    }

    /**
     * RF-59: registra la salida de un bien. La fecha estimada de devolucion es
     * opcional, pero no puede ser anterior a hoy.
     */
    public static Prestamo registrar(BienPrestado bien,
                                     PersonaResponsable persona,
                                     String destino,
                                     LocalDate fechaEstimadaDevolucion,
                                     String observacionesSalida,
                                     OperadorPrestamo usuarioPresta) {

        if (fechaEstimadaDevolucion != null && fechaEstimadaDevolucion.isBefore(LocalDate.now())) {
            throw new DatosInvalidosException("fechaEstimadaDevolucion",
                    "La fecha estimada de devolucion no puede ser anterior a hoy.");
        }

        Prestamo prestamo = new Prestamo();
        prestamo.bien = bien;
        prestamo.persona = persona;
        prestamo.destino = normalizar(destino, MAX_DESTINO, "destino");
        prestamo.fechaPrestamo = LocalDateTime.now();
        prestamo.fechaEstimadaDevolucion = fechaEstimadaDevolucion;
        prestamo.observacionesSalida = normalizar(observacionesSalida, MAX_OBSERVACIONES, "observacionesSalida");
        prestamo.usuarioPresta = usuarioPresta;
        prestamo.estado = EstadoPrestamo.ACTIVO;
        return prestamo;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Prestamo reconstituir(Long id,
                                        BienPrestado bien,
                                        PersonaResponsable persona,
                                        String destino,
                                        LocalDateTime fechaPrestamo,
                                        LocalDate fechaEstimadaDevolucion,
                                        String observacionesSalida,
                                        LocalDateTime fechaDevolucion,
                                        Boolean conforme,
                                        boolean reportaDano,
                                        String observacionesRetorno,
                                        OperadorPrestamo usuarioPresta,
                                        OperadorPrestamo usuarioRecibe,
                                        EstadoPrestamo estado) {
        Prestamo prestamo = new Prestamo();
        prestamo.id = id;
        prestamo.bien = bien;
        prestamo.persona = persona;
        prestamo.destino = destino;
        prestamo.fechaPrestamo = fechaPrestamo;
        prestamo.fechaEstimadaDevolucion = fechaEstimadaDevolucion;
        prestamo.observacionesSalida = observacionesSalida;
        prestamo.fechaDevolucion = fechaDevolucion;
        prestamo.conforme = conforme;
        prestamo.reportaDano = reportaDano;
        prestamo.observacionesRetorno = observacionesRetorno;
        prestamo.usuarioPresta = usuarioPresta;
        prestamo.usuarioRecibe = usuarioRecibe;
        prestamo.estado = estado;
        return prestamo;
    }

    /**
     * RF-61, RF-65: registra la devolucion previa validacion de conformidad.
     *
     * <p>Si el bien no se recibe conforme se exige detallar su estado. A
     * diferencia de la version 1.0, el dano NO envia el bien a mantenimiento:
     * lo marca para revision y la decision queda en manos del Responsable
     * (RN-19). Un Operador nunca retira un bien del servicio por si mismo.</p>
     */
    public void registrarDevolucion(boolean conforme,
                                    boolean reportaDano,
                                    String observacionesRetorno,
                                    OperadorPrestamo usuarioRecibe) {

        if (estado == EstadoPrestamo.DEVUELTO) {
            throw new ReglaNegocioException("Este prestamo ya fue devuelto el " + fechaDevolucion + ".");
        }
        if (!conforme && (observacionesRetorno == null || observacionesRetorno.isBlank())) {
            throw new DatosInvalidosException("observacionesRetorno",
                    "Si la devolucion no es conforme debe detallar el estado en que se recibe el bien.");
        }

        this.conforme = conforme;
        this.reportaDano = reportaDano || !conforme;
        this.observacionesRetorno = normalizar(observacionesRetorno, MAX_OBSERVACIONES, "observacionesRetorno");
        this.fechaDevolucion = LocalDateTime.now();
        this.usuarioRecibe = usuarioRecibe;
        this.estado = EstadoPrestamo.DEVUELTO;
    }

    /** Coordinacion a la que pertenece el prestamo, heredada del bien (RN-23). */
    public Long coordinacionId() {
        return bien == null ? null : bien.coordinacionId();
    }

    public boolean perteneceA(Long coordinacion) {
        Long propia = coordinacionId();
        return propia != null && propia.equals(coordinacion);
    }

    /** RF-67: prestamo activo que supero la fecha estimada de devolucion. */
    public boolean estaVencido() {
        return estado == EstadoPrestamo.ACTIVO
                && fechaEstimadaDevolucion != null
                && fechaEstimadaDevolucion.isBefore(LocalDate.now());
    }

    public long diasAtraso() {
        if (!estaVencido()) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(fechaEstimadaDevolucion, LocalDate.now());
    }

    public boolean estaActivo() {
        return estado == EstadoPrestamo.ACTIVO;
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

    // ------------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public BienPrestado getBien() {
        return bien;
    }

    public PersonaResponsable getPersona() {
        return persona;
    }

    public String getDestino() {
        return destino;
    }

    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }

    public LocalDate getFechaEstimadaDevolucion() {
        return fechaEstimadaDevolucion;
    }

    public String getObservacionesSalida() {
        return observacionesSalida;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public Boolean getConforme() {
        return conforme;
    }

    /** RN-19: se devolvio con dano; el Responsable debe revisarlo. */
    public boolean isReportaDano() {
        return reportaDano;
    }

    public String getObservacionesRetorno() {
        return observacionesRetorno;
    }

    public OperadorPrestamo getUsuarioPresta() {
        return usuarioPresta;
    }

    public OperadorPrestamo getUsuarioRecibe() {
        return usuarioRecibe;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }
}
