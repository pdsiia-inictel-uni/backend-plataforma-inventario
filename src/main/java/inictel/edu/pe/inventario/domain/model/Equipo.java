package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Raiz del agregado Bien (RF-34 .. RF-45).
 *
 * <p>Concentra las reglas patrimoniales del bien: a que Coordinacion pertenece
 * y no puede salir (RN-11), en que condicion nace (RN-13), que transiciones
 * admite y como se da de baja sin borrarlo (RNF-47).</p>
 *
 * <p>Los atributos dinamicos de la version 1.0 desaparecen: el bien tiene un
 * conjunto de campos fijos, lo que simplifica el formulario para el Operador,
 * que es el usuario con menos experiencia del sistema (RNF-22).</p>
 */
public class Equipo {

    private static final int MAX_NOMBRE = 150;
    private static final int MAX_TEXTO = 100;
    private static final int MAX_OBSERVACIONES = 1000;
    private static final int MAX_MOTIVO = 500;
    /** RN-25: no se registran bienes anteriores a esta fecha. */
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1980, 1, 1);

    private Long id;
    private Long coordinacionId;
    private Long laboratorioId;
    private String nombre;
    private String marca;
    private String modelo;
    private NumeroSerie numeroSerie;
    private CodigoBien codigoInventario;
    private CodigoBien codigoPatrimonial;
    private ReferenciaCategoria categoria;
    private CondicionEquipo condicion;
    private LocalDate fechaAdquisicion;
    private BigDecimal costo;
    private String observaciones;
    private String fotoUrl;
    private boolean revisionPendiente;
    private String motivoBaja;
    private LocalDateTime fechaBaja;
    private Long responsableId;
    /**
     * RF-83: Operador que tiene el bien a su cargo.
     *
     * <p>{@code null} no es un hueco sin rellenar: significa que lo lleva el
     * <b>Responsable de la Coordinacion</b>, sea quien sea en cada momento. Es
     * como nace todo bien (RF-36) y es donde vuelve cuando el Responsable lo
     * retira de manos de un Operador (RN-37).</p>
     */
    private Long responsableEquipoId;
    private Long usuarioRegistroId;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private boolean activo;

    private Equipo() {
    }

    /**
     * RF-34, RF-35: registro de un bien.
     *
     * <p>La condicion no se recibe: todo bien nace OPERATIVO. La Coordinacion y
     * el responsable tampoco los elige el cliente, los impone el servidor a
     * partir de quien registra (RF-36, RF-37).</p>
     */
    public static Equipo registrar(Long coordinacionId,
                                   Long laboratorioId,
                                   String nombre,
                                   String marca,
                                   String modelo,
                                   NumeroSerie numeroSerie,
                                   CodigoBien codigoInventario,
                                   CodigoBien codigoPatrimonial,
                                   ReferenciaCategoria categoria,
                                   LocalDate fechaAdquisicion,
                                   BigDecimal costo,
                                   String observaciones,
                                   Long responsableId,
                                   Long usuarioRegistroId) {

        Equipo equipo = new Equipo();
        equipo.coordinacionId = exigirCoordinacion(coordinacionId);
        equipo.laboratorioId = laboratorioId;
        equipo.nombre = exigirTexto(nombre, MAX_NOMBRE, "nombre", "Ingrese el nombre del equipo.");
        equipo.marca = exigirTexto(marca, MAX_TEXTO, "marca", "Ingrese la marca del equipo.");
        equipo.modelo = exigirTexto(modelo, MAX_TEXTO, "modelo", "Ingrese el modelo del equipo.");
        equipo.numeroSerie = numeroSerie;
        equipo.codigoInventario = codigoInventario;
        equipo.codigoPatrimonial = codigoPatrimonial;
        equipo.categoria = exigirCategoria(categoria);
        equipo.fechaAdquisicion = exigirFecha(fechaAdquisicion);
        equipo.costo = exigirCosto(costo);
        equipo.observaciones = normalizar(observaciones, MAX_OBSERVACIONES, "observaciones");
        equipo.responsableId = responsableId;
        // RF-83: el bien nace a cargo del Responsable de la Coordinacion, lo
        // registre quien lo registre. Que un Operador lo de de alta no lo pone
        // a su nombre: eso lo decide despues el Responsable (RN-37).
        equipo.responsableEquipoId = null;
        equipo.usuarioRegistroId = usuarioRegistroId;
        // RN-13: sin excepciones. La condicion se informa, no se pregunta.
        equipo.condicion = CondicionEquipo.OPERATIVO;
        equipo.revisionPendiente = false;
        equipo.activo = true;
        equipo.fechaRegistro = LocalDateTime.now();
        return equipo;
    }

    /** Reconstruccion desde la persistencia. Uso exclusivo de los adaptadores. */
    public static Equipo reconstituir(Long id,
                                      Long coordinacionId,
                                      Long laboratorioId,
                                      String nombre,
                                      String marca,
                                      String modelo,
                                      NumeroSerie numeroSerie,
                                      CodigoBien codigoInventario,
                                      CodigoBien codigoPatrimonial,
                                      ReferenciaCategoria categoria,
                                      CondicionEquipo condicion,
                                      LocalDate fechaAdquisicion,
                                      BigDecimal costo,
                                      String observaciones,
                                      String fotoUrl,
                                      boolean revisionPendiente,
                                      String motivoBaja,
                                      LocalDateTime fechaBaja,
                                      Long responsableId,
                                      Long responsableEquipoId,
                                      Long usuarioRegistroId,
                                      LocalDateTime fechaRegistro,
                                      LocalDateTime fechaActualizacion,
                                      boolean activo) {
        Equipo equipo = new Equipo();
        equipo.id = id;
        equipo.coordinacionId = coordinacionId;
        equipo.laboratorioId = laboratorioId;
        equipo.nombre = nombre;
        equipo.marca = marca;
        equipo.modelo = modelo;
        equipo.numeroSerie = numeroSerie;
        equipo.codigoInventario = codigoInventario;
        equipo.codigoPatrimonial = codigoPatrimonial;
        equipo.categoria = categoria;
        equipo.condicion = condicion;
        equipo.fechaAdquisicion = fechaAdquisicion;
        equipo.costo = costo;
        equipo.observaciones = observaciones;
        equipo.fotoUrl = fotoUrl;
        equipo.revisionPendiente = revisionPendiente;
        equipo.motivoBaja = motivoBaja;
        equipo.fechaBaja = fechaBaja;
        equipo.responsableId = responsableId;
        equipo.responsableEquipoId = responsableEquipoId;
        equipo.usuarioRegistroId = usuarioRegistroId;
        equipo.fechaRegistro = fechaRegistro;
        equipo.fechaActualizacion = fechaActualizacion;
        equipo.activo = activo;
        return equipo;
    }

    // ------------------------------------------------------------------
    // Comportamiento
    // ------------------------------------------------------------------

    /**
     * RF-40: edicion de los datos base. La condicion, las fechas y la
     * Coordinacion quedan fuera: no son editables por nadie.
     */
    public void actualizarDatos(Long laboratorioId,
                                String nombre,
                                String marca,
                                String modelo,
                                NumeroSerie numeroSerie,
                                CodigoBien codigoInventario,
                                CodigoBien codigoPatrimonial,
                                ReferenciaCategoria categoria,
                                LocalDate fechaAdquisicion,
                                BigDecimal costo,
                                String observaciones) {
        if (!activo) {
            throw new ReglaNegocioException(
                    "El bien esta dado de baja; para modificarlo debe reincorporarlo al inventario.");
        }
        this.laboratorioId = laboratorioId;
        this.nombre = exigirTexto(nombre, MAX_NOMBRE, "nombre", "Ingrese el nombre del equipo.");
        this.marca = exigirTexto(marca, MAX_TEXTO, "marca", "Ingrese la marca del equipo.");
        this.modelo = exigirTexto(modelo, MAX_TEXTO, "modelo", "Ingrese el modelo del equipo.");
        this.numeroSerie = numeroSerie;
        this.codigoInventario = codigoInventario;
        this.codigoPatrimonial = codigoPatrimonial;
        this.categoria = exigirCategoria(categoria);
        this.fechaAdquisicion = exigirFecha(fechaAdquisicion);
        this.costo = exigirCosto(costo);
        this.observaciones = normalizar(observaciones, MAX_OBSERVACIONES, "observaciones");
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-41: el Responsable retira el bien del servicio. */
    public void enviarAMantenimiento(String motivo) {
        exigirActivo();
        if (condicion == CondicionEquipo.PRESTADO) {
            throw new ReglaNegocioException(
                    "El bien esta prestado. Registre su devolucion antes de enviarlo a mantenimiento.");
        }
        if (condicion == CondicionEquipo.MANTENIMIENTO) {
            throw new ReglaNegocioException("El bien ya se encuentra en mantenimiento.");
        }
        this.condicion = CondicionEquipo.MANTENIMIENTO;
        // El motivo del envio queda en el movimiento; aqui solo se cierra la alerta.
        this.revisionPendiente = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-41: el bien vuelve al servicio tras la reparacion o la revision. */
    public void devolverAOperativo() {
        exigirActivo();
        if (condicion == CondicionEquipo.PRESTADO) {
            throw new ReglaNegocioException(
                    "El bien esta prestado. Su condicion se actualizara al registrar la devolucion.");
        }
        if (condicion == CondicionEquipo.OPERATIVO && !revisionPendiente) {
            throw new ReglaNegocioException("El bien ya se encuentra operativo.");
        }
        this.condicion = CondicionEquipo.OPERATIVO;
        this.revisionPendiente = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-42, RNF-47: baja logica con motivo; el bien se conserva en el historico. */
    public void darDeBaja(String motivo) {
        if (!activo) {
            throw new ReglaNegocioException("El bien ya se encuentra dado de baja.");
        }
        if (condicion == CondicionEquipo.PRESTADO) {
            throw new ReglaNegocioException(
                    "No se puede dar de baja un bien prestado. Registre primero su devolucion.");
        }
        String limpio = motivo == null ? "" : motivo.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException("motivo", "Indique el motivo de la baja.");
        }
        if (limpio.length() > MAX_MOTIVO) {
            throw new DatosInvalidosException("motivo", "El motivo no puede superar los 500 caracteres.");
        }
        this.activo = false;
        this.condicion = CondicionEquipo.BAJA;
        this.revisionPendiente = false;
        // RN-37: un bien fuera de servicio no esta a cargo de nadie. Sin esto,
        // el Operador que lo tenia quedaria atado a el para siempre: no podria
        // dejar su puesto por un equipo que ya no existe (RN-38).
        this.responsableEquipoId = null;
        this.motivoBaja = limpio;
        this.fechaBaja = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-43: el bien regresa al inventario operativo. */
    public void reincorporar() {
        if (activo) {
            throw new ReglaNegocioException("El bien ya se encuentra activo en el inventario.");
        }
        this.activo = true;
        this.condicion = CondicionEquipo.OPERATIVO;
        this.motivoBaja = null;
        this.fechaBaja = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-60: el bien pasa a Prestado al registrarse una salida. */
    public void marcarComoPrestado() {
        if (!activo) {
            throw new ReglaNegocioException("El bien esta dado de baja y no puede prestarse.");
        }
        // RN-15: solo se presta lo que esta operativo.
        if (condicion != CondicionEquipo.OPERATIVO) {
            throw new ReglaNegocioException(
                    "El bien no esta disponible (condicion actual: " + condicion.getEtiqueta() + ").");
        }
        this.condicion = CondicionEquipo.PRESTADO;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-63, RF-64: al devolverse, el bien vuelve siempre a OPERATIVO.
     *
     * <p>Si la devolucion reporta dano NO se envia solo a mantenimiento: queda
     * marcado para revision, y es el Responsable quien decide (RN-19). Asi un
     * Operador nunca retira un bien del servicio por su cuenta.</p>
     */
    public void registrarRetorno(boolean reportaDano) {
        this.condicion = CondicionEquipo.OPERATIVO;
        this.revisionPendiente = reportaDano;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-51: fotografia del bien. */
    public void asignarFoto(String fotoUrl) {
        this.fotoUrl = fotoUrl;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-36: el bien queda a cargo del Responsable vigente de su Coordinacion. */
    public void asignarResponsable(Long responsableId) {
        this.responsableId = responsableId;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-83: pone el bien a cargo de un Operador de su Coordinacion.
     *
     * <p>Quien lo hace es siempre el Responsable, y el servicio comprueba que
     * el destinatario sea de verdad un Operador suyo: el agregado no conoce a
     * las personas, solo sus identificadores (RNF-39).</p>
     *
     * <p>Un bien dado de baja no admite responsable: ya no esta en servicio y
     * nadie tiene que responder por el uso de algo que no se usa (RN-37).</p>
     */
    public void ponerACargoDe(Long operadorId) {
        exigirActivo();
        if (operadorId == null) {
            throw new DatosInvalidosException("responsableEquipoId",
                    "Indique a quien se le entrega el equipo.");
        }
        if (operadorId.equals(responsableEquipoId)) {
            throw new ReglaNegocioException("El equipo ya esta a cargo de esa persona.");
        }
        this.responsableEquipoId = operadorId;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /**
     * RF-83: el bien vuelve a cargo del Responsable de la Coordinacion.
     *
     * <p>Es lo que el Responsable hace al recuperar un equipo que tenia un
     * Operador —y lo que ocurre solo cuando ese Operador deja el puesto o
     * cuando el bien se da de baja—. No queda "sin responsable": queda a
     * nombre de quien responde por la Coordinacion entera (RN-37).</p>
     */
    public void devolverAlResponsableDeCoordinacion() {
        this.responsableEquipoId = null;
        this.fechaActualizacion = LocalDateTime.now();
    }

    /** RF-83: hay un Operador con este bien a su nombre. */
    public boolean estaACargoDeUnOperador() {
        return responsableEquipoId != null;
    }

    public boolean estaDisponible() {
        return activo && condicion == CondicionEquipo.OPERATIVO;
    }

    /** RN-23: base del aislamiento por Coordinacion. */
    public boolean perteneceA(Long coordinacion) {
        return coordinacionId != null && coordinacionId.equals(coordinacion);
    }

    // ------------------------------------------------------------------
    // Validaciones internas
    // ------------------------------------------------------------------

    private void exigirActivo() {
        if (!activo) {
            throw new ReglaNegocioException("El bien esta dado de baja.");
        }
    }

    private static Long exigirCoordinacion(Long coordinacionId) {
        if (coordinacionId == null) {
            throw new DatosInvalidosException("coordinacionId",
                    "El bien debe pertenecer a una coordinacion.");
        }
        return coordinacionId;
    }

    private static ReferenciaCategoria exigirCategoria(ReferenciaCategoria categoria) {
        if (categoria == null) {
            throw new DatosInvalidosException("categoriaId", "Seleccione la categoria del bien.");
        }
        return categoria;
    }

    /**
     * RN-25: la fecha de adquisicion no puede ser futura.
     *
     * <p>Desde la v3.10 es una fecha y no un anio: el formato de registro de
     * uso pide la de la orden de compra (RF-78), y de un anio no se saca una
     * fecha. El limite inferior lo comparte con la base, que lo declara en un
     * CHECK; el superior solo puede vivir aqui, porque depende del dia.</p>
     */
    private static LocalDate exigirFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new DatosInvalidosException("fechaAdquisicion", "Ingrese la fecha de adquisicion.");
        }
        if (fecha.isBefore(FECHA_MINIMA)) {
            throw new DatosInvalidosException("fechaAdquisicion",
                    "La fecha de adquisicion no puede ser anterior al " + FECHA_MINIMA + ".");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new DatosInvalidosException("fechaAdquisicion",
                    "La fecha de adquisicion no puede ser futura.");
        }
        return fecha;
    }

    /** RN-25: el costo no puede ser negativo. */
    private static BigDecimal exigirCosto(BigDecimal costo) {
        if (costo == null) {
            throw new DatosInvalidosException("costo", "Ingrese el costo del equipo.");
        }
        if (costo.signum() < 0) {
            throw new DatosInvalidosException("costo", "El costo no puede ser negativo.");
        }
        return costo;
    }

    private static String exigirTexto(String valor, int maximo, String campo, String mensaje) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.isEmpty()) {
            throw new DatosInvalidosException(campo, mensaje);
        }
        if (limpio.length() > maximo) {
            throw new DatosInvalidosException(campo,
                    "El texto no puede superar los " + maximo + " caracteres.");
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

    // ------------------------------------------------------------------
    // Estado
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void asignarId(Long id) {
        this.id = id;
    }

    public Long getCoordinacionId() {
        return coordinacionId;
    }

    public Long getLaboratorioId() {
        return laboratorioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public NumeroSerie getNumeroSerie() {
        return numeroSerie;
    }

    public CodigoBien getCodigoInventario() {
        return codigoInventario;
    }

    public CodigoBien getCodigoPatrimonial() {
        return codigoPatrimonial;
    }

    public ReferenciaCategoria getCategoria() {
        return categoria;
    }

    public CondicionEquipo getCondicion() {
        return condicion;
    }

    public LocalDate getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public boolean isRevisionPendiente() {
        return revisionPendiente;
    }

    public String getMotivoBaja() {
        return motivoBaja;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    public Long getResponsableId() {
        return responsableId;
    }

    /** RF-83: {@code null} = a cargo del Responsable de la Coordinacion. */
    public Long getResponsableEquipoId() {
        return responsableEquipoId;
    }

    public Long getUsuarioRegistroId() {
        return usuarioRegistroId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }
}
