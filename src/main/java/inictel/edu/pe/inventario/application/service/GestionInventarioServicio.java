package inictel.edu.pe.inventario.application.service;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.domain.Pagina;
import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.inventario.application.comando.GuardarEquipoComando;
import inictel.edu.pe.inventario.application.dto.EquipoDto;
import inictel.edu.pe.inventario.application.dto.EquipoResumenDto;
import inictel.edu.pe.inventario.application.dto.MovimientoDto;
import inictel.edu.pe.inventario.application.dto.ResponsableEquipoDto;
import inictel.edu.pe.inventario.domain.model.Categoria;
import inictel.edu.pe.inventario.domain.model.CodigoBien;
import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.Equipo;
import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;
import inictel.edu.pe.inventario.domain.model.NumeroSerie;
import inictel.edu.pe.inventario.domain.model.ReferenciaCategoria;
import inictel.edu.pe.inventario.domain.model.TipoMovimiento;
import inictel.edu.pe.inventario.domain.repository.CategoriaRepositorio;
import inictel.edu.pe.inventario.domain.repository.EquipoRepositorio;
import inictel.edu.pe.inventario.domain.repository.FiltroEquipos;
import inictel.edu.pe.inventario.domain.repository.MovimientoRepositorio;
import inictel.edu.pe.inventario.domain.service.AlmacenFotos;
import inictel.edu.pe.inventario.domain.service.DirectorioUsuarios;
import inictel.edu.pe.inventario.domain.service.UbicacionesDisponibles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Casos de uso del inventario de bienes (RF-34 .. RF-57).
 *
 * <p>Dos responsabilidades atraviesan todo el servicio:</p>
 * <ul>
 *   <li><b>Aislamiento</b> (RN-23): ninguna operacion toca un bien de otra
 *       Coordinacion. El identificador de Coordinacion jamas llega del cliente:
 *       se deduce del usuario autenticado.</li>
 *   <li><b>Trazabilidad</b> (RN-20): toda transicion de condicion deja un
 *       movimiento inmutable en la linea de tiempo del bien.</li>
 * </ul>
 */
@Service
public class GestionInventarioServicio {

    private final EquipoRepositorio equipos;
    private final MovimientoRepositorio movimientos;
    private final CategoriaRepositorio categorias;
    private final DirectorioUsuarios directorio;
    private final UbicacionesDisponibles ubicaciones;
    private final AlmacenFotos almacenFotos;
    private final ContextoUsuario contexto;

    public GestionInventarioServicio(EquipoRepositorio equipos,
                                     MovimientoRepositorio movimientos,
                                     CategoriaRepositorio categorias,
                                     DirectorioUsuarios directorio,
                                     UbicacionesDisponibles ubicaciones,
                                     AlmacenFotos almacenFotos,
                                     ContextoUsuario contexto) {
        this.equipos = equipos;
        this.movimientos = movimientos;
        this.categorias = categorias;
        this.directorio = directorio;
        this.ubicaciones = ubicaciones;
        this.almacenFotos = almacenFotos;
        this.contexto = contexto;
    }

    // ------------------------------------------------------------------
    // Consultas (RF-47 .. RF-50)
    // ------------------------------------------------------------------

    /**
     * RF-47, RF-49, RF-84: listado paginado.
     *
     * <p>El Responsable y el Operador ven siempre su Coordinacion, envien lo que
     * envien. El Administrador debe elegir cual consultar (RF-49); sin eleccion
     * ve toda la institucion.</p>
     *
     * <p>Cada fila dice quien tiene el bien a su cargo, que es lo que convierte
     * el listado en un reparto y no solo en un catalogo (RF-83). Los nombres se
     * resuelven contra una memoria de la propia pagina: sin ella, una tabla de
     * cincuenta equipos de tres operadores pedia cincuenta veces los mismos
     * tres nombres (RNF-12).</p>
     */
    @Transactional(readOnly = true)
    public Pagina<EquipoResumenDto> buscar(FiltroEquipos filtro, CriterioPagina criterio) {
        Map<Long, String> nombres = new HashMap<>();
        return equipos.buscar(acotar(filtro), criterio)
                .mapear(equipo -> EquipoResumenDto.de(equipo,
                        ubicaciones.nombreDeLaboratorio(equipo.getLaboratorioId()).orElse(null),
                        nombreDeQuienLoTieneACargo(equipo, nombres)));
    }

    /**
     * RF-84: quien lleva equipos en la Coordinacion, y cuantos lleva cada uno.
     *
     * <p>Es lo que hace posible el listado por responsable de equipo: sin este
     * reparto, elegir "los equipos de fulano" obligaria a recorrer la lista de
     * operadores de la Coordinacion probando uno por uno cual tiene algo. La
     * primera opcion es siempre la del Responsable —los bienes sin asignacion,
     * que son suyos por definicion (RN-37)— y las demas van por apellido.</p>
     *
     * <p>El Administrador consulta la Coordinacion que elija y esta obligado a
     * elegir una: el reparto de bienes es un asunto interno de cada
     * Coordinacion y mezclarlas juntaria en una sola fila a los responsables de
     * todas ellas (RF-49, RN-23).</p>
     */
    @Transactional(readOnly = true)
    public List<ResponsableEquipoDto> responsablesDeEquipo(Long coordinacionId) {
        UsuarioAutenticado actual = contexto.requerido();
        Long ambito = actual.ambitoDeConsulta(coordinacionId);
        if (ambito == null) {
            throw new DatosInvalidosException("coordinacionId",
                    "Elija la coordinacion cuyo reparto de equipos desea consultar.");
        }

        Long responsableCoordinacion = directorio.responsableVigenteDe(ambito).orElse(null);

        return equipos.contarPorResponsableDeEquipo(ambito).stream()
                .map(conteo -> componerResponsable(conteo, responsableCoordinacion))
                .sorted(Comparator
                        .comparing(ResponsableEquipoDto::esResponsableCoordinacion).reversed()
                        .thenComparing(ResponsableEquipoDto::nombreCompleto,
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Una fila del reparto. La del identificador nulo es la del Responsable de
     * la Coordinacion: sus bienes son los que no tienen asignacion (RN-37).
     */
    private ResponsableEquipoDto componerResponsable(EquipoRepositorio.ConteoACargo conteo,
                                                     Long responsableCoordinacion) {
        if (conteo.responsableEquipoId() == null) {
            String nombre = directorio.nombreDe(responsableCoordinacion)
                    // RN-07: una Coordinacion puede estar parada, sin responsable
                    // nombrado, y sus bienes siguen ahi esperando a quien llegue.
                    .orElse("Responsable de la coordinacion");
            return new ResponsableEquipoDto(null, nombre, true, conteo.cantidad());
        }
        String nombre = directorio.nombreDe(conteo.responsableEquipoId())
                .orElse("Operador #" + conteo.responsableEquipoId());
        return new ResponsableEquipoDto(conteo.responsableEquipoId(), nombre, false, conteo.cantidad());
    }

    /**
     * RF-83: quien responde por el bien hoy, con memoria de los ya resueltos.
     *
     * <p>El Operador a su cargo si lo tiene; si no, el Responsable vigente de
     * la Coordinacion, que es lo que significa no tenerlo (RN-37).</p>
     */
    private String nombreDeQuienLoTieneACargo(Equipo equipo, Map<Long, String> memoria) {
        if (equipo.estaACargoDeUnOperador()) {
            return memoria.computeIfAbsent(equipo.getResponsableEquipoId(),
                    id -> directorio.nombreDe(id).orElse(null));
        }
        // Clave negativa: la coordinacion y el usuario comparten memoria, y un
        // identificador de coordinacion podria coincidir con uno de persona.
        return memoria.computeIfAbsent(-equipo.getCoordinacionId(),
                id -> directorio.responsableVigenteDe(-id)
                        .flatMap(directorio::nombreDe)
                        .orElse(null));
    }

    @Transactional(readOnly = true)
    public EquipoDto obtener(Long id) {
        Equipo equipo = exigirEquipo(id);
        exigirLectura(equipo);
        return componer(equipo);
    }

    /** RF-56, RF-57: linea de tiempo completa del bien. */
    @Transactional(readOnly = true)
    public List<MovimientoDto> historial(Long id) {
        Equipo equipo = exigirEquipo(id);
        exigirLectura(equipo);
        return movimientos.historialDe(id).stream().map(MovimientoDto::de).toList();
    }

    // ------------------------------------------------------------------
    // RF-34 .. RF-37: registro
    // ------------------------------------------------------------------

    /**
     * Registra un bien en la Coordinacion del usuario en curso.
     *
     * <p>El bien nace OPERATIVO y a cargo del Responsable vigente de la
     * Coordinacion, aunque quien complete el formulario sea un Operador
     * (RF-35, RF-36).</p>
     */
    @Transactional
    public EquipoDto crear(GuardarEquipoComando comando) {
        UsuarioAutenticado actual = exigirOperativo();
        Long coordinacionId = actual.coordinacionRequerida();

        // RN-26: sin laboratorios no hay donde poner el bien, y un bien que no
        // se sabe donde esta no sirve de nada en un inventario.
        if (!ubicaciones.tieneLaboratoriosActivos(coordinacionId)) {
            throw new ReglaNegocioException(
                    "Su coordinacion aun no tiene laboratorios activos. "
                            + "Pida al Administrador que registre al menos uno antes de dar de alta equipos.");
        }

        NumeroSerie numeroSerie = new NumeroSerie(comando.numeroSerie());
        CodigoBien codigoInventario = CodigoBien.inventario(comando.codigoInventario());
        CodigoBien codigoPatrimonial = CodigoBien.patrimonial(comando.codigoPatrimonial());

        validarUnicidad(numeroSerie, codigoInventario, codigoPatrimonial, null);
        ReferenciaCategoria categoria = resolverCategoria(comando.categoriaId(), null);
        Long laboratorioId = resolverLaboratorio(comando.laboratorioId(), coordinacionId);

        // RF-36: el responsable es el del cargo, no el que teclea. RN-07: si el
        // cargo esta vacante no hay quien responda por el equipo, asi que la
        // coordinacion no registra hasta que se nombre a otro. Es lo que anuncia
        // su tarjeta en rojo desde que se le da de baja al anterior (RF-26).
        Long responsableId = directorio.responsableVigenteDe(coordinacionId)
                .orElseThrow(() -> new ReglaNegocioException(
                        "Su coordinacion no tiene responsable en este momento, asi que no hay quien "
                                + "responda por los equipos que se registren. Pida al Administrador "
                                + "que nombre uno."));

        Equipo equipo = Equipo.registrar(
                coordinacionId,
                laboratorioId,
                comando.nombre(),
                comando.marca(),
                comando.modelo(),
                numeroSerie,
                codigoInventario,
                codigoPatrimonial,
                categoria,
                comando.fechaAdquisicion(),
                comando.costo(),
                comando.observaciones(),
                responsableId,
                actual.id());

        Equipo guardado = equipos.guardar(equipo);

        registrarMovimiento(guardado, TipoMovimiento.ALTA, null,
                "Alta del bien en el inventario. Queda en condicion Operativo.");

        return componer(guardado);
    }

    // ------------------------------------------------------------------
    // RF-40: edicion
    // ------------------------------------------------------------------

    @Transactional
    public EquipoDto editar(GuardarEquipoComando comando) {
        Equipo equipo = exigirEquipo(comando.id());
        exigirResponsableDe(equipo);

        NumeroSerie numeroSerie = new NumeroSerie(comando.numeroSerie());
        CodigoBien codigoInventario = CodigoBien.inventario(comando.codigoInventario());
        CodigoBien codigoPatrimonial = CodigoBien.patrimonial(comando.codigoPatrimonial());

        validarUnicidad(numeroSerie, codigoInventario, codigoPatrimonial, equipo.getId());
        ReferenciaCategoria categoria = resolverCategoria(comando.categoriaId(), equipo.getCategoria());
        Long laboratorioId = resolverLaboratorio(comando.laboratorioId(), equipo.getCoordinacionId());

        Long laboratorioAnterior = equipo.getLaboratorioId();

        equipo.actualizarDatos(laboratorioId, comando.nombre(), comando.marca(), comando.modelo(),
                numeroSerie, codigoInventario, codigoPatrimonial, categoria,
                comando.fechaAdquisicion(), comando.costo(), comando.observaciones());

        Equipo guardado = equipos.guardar(equipo);

        // Una reubicacion es un hecho distinto de una edicion: se registra aparte.
        boolean reubicado = laboratorioAnterior == null
                ? laboratorioId != null
                : !laboratorioAnterior.equals(laboratorioId);
        if (reubicado) {
            registrarMovimiento(guardado, TipoMovimiento.REUBICACION, guardado.getCondicion(),
                    "Reubicado en " + nombreLaboratorioODefecto(laboratorioId) + ".");
        }
        registrarMovimiento(guardado, TipoMovimiento.EDICION, guardado.getCondicion(),
                "Actualizacion de los datos del bien.");

        return componer(guardado);
    }

    // ------------------------------------------------------------------
    // RF-83: responsable del equipo
    // ------------------------------------------------------------------

    /**
     * Pone el bien a cargo de un Operador de la Coordinacion, o lo devuelve a
     * cargo del Responsable.
     *
     * <p>Es una decision del <b>Responsable</b> y solo suya: es quien responde
     * por el inventario entero y quien reparte el trabajo dentro de su
     * Coordinacion. Un Operador no se asigna equipos a si mismo ni se los pasa
     * a un companero (RF-83, RN-37).</p>
     *
     * <p>{@code operadorId} nulo significa <em>devolver el bien al
     * Responsable</em>: es lo que el usuario ve como "me lo quedo yo" y lo que
     * hay que hacer antes de que un Operador con equipos a su nombre pueda
     * dejar su puesto (RN-38).</p>
     */
    @Transactional
    public EquipoDto asignarResponsableDeEquipo(Long equipoId, Long operadorId) {
        Equipo equipo = exigirEquipo(equipoId);
        exigirResponsableDe(equipo);

        String anterior = nombreDeQuienLoTieneACargo(equipo);

        if (operadorId == null) {
            if (!equipo.estaACargoDeUnOperador()) {
                throw new ReglaNegocioException(
                        "El equipo ya esta a su cargo como responsable de la coordinacion.");
            }
            equipo.devolverAlResponsableDeCoordinacion();
        } else {
            // RN-37: solo un Operador activo de esta misma Coordinacion. Se
            // comprueba aqui y no en el agregado porque el bien no conoce a las
            // personas, solo sus identificadores (RNF-39).
            if (!directorio.esOperadorActivoDe(operadorId, equipo.getCoordinacionId())) {
                throw new DatosInvalidosException("responsableEquipoId",
                        "Un equipo solo se entrega a un operador activo de su coordinacion.");
            }
            equipo.ponerACargoDe(operadorId);
        }

        Equipo guardado = equipos.guardar(equipo);
        String ahora = nombreDeQuienLoTieneACargo(guardado);

        registrarMovimiento(guardado, TipoMovimiento.RESPONSABLE, guardado.getCondicion(),
                "El equipo pasa de " + anterior + " a " + ahora + ".");

        return componer(guardado);
    }

    // ------------------------------------------------------------------
    // RF-41 .. RF-43: condicion, baja y reincorporacion
    // ------------------------------------------------------------------

    /** RF-41: el Responsable retira el bien del servicio. */
    @Transactional
    public EquipoDto enviarAMantenimiento(Long id, String motivo) {
        Equipo equipo = exigirEquipo(id);
        exigirResponsableDe(equipo);

        CondicionEquipo anterior = equipo.getCondicion();
        equipo.enviarAMantenimiento(motivo);
        Equipo guardado = equipos.guardar(equipo);

        String detalle = motivo == null || motivo.isBlank()
                ? "Enviado a mantenimiento."
                : "Enviado a mantenimiento. Motivo: " + motivo.trim();
        registrarMovimiento(guardado, TipoMovimiento.MANTENIMIENTO, anterior, detalle);

        return componer(guardado);
    }

    /** RF-41: el bien vuelve al servicio, o se cierra la alerta de revision. */
    @Transactional
    public EquipoDto devolverAOperativo(Long id, String observacion) {
        Equipo equipo = exigirEquipo(id);
        exigirResponsableDe(equipo);

        CondicionEquipo anterior = equipo.getCondicion();
        boolean cerrabaRevision = equipo.isRevisionPendiente();
        equipo.devolverAOperativo();
        Equipo guardado = equipos.guardar(equipo);

        String base = cerrabaRevision
                ? "Revision conforme tras la devolucion con observaciones."
                : "Devuelto a condicion operativa.";
        String detalle = observacion == null || observacion.isBlank()
                ? base
                : base + " " + observacion.trim();
        registrarMovimiento(guardado, TipoMovimiento.OPERATIVO, anterior, detalle);

        return componer(guardado);
    }

    /** RF-42: baja logica con motivo. */
    @Transactional
    public EquipoDto darDeBaja(Long id, String motivo) {
        Equipo equipo = exigirEquipo(id);
        exigirResponsableDe(equipo);

        CondicionEquipo anterior = equipo.getCondicion();
        equipo.darDeBaja(motivo);
        Equipo guardado = equipos.guardar(equipo);

        registrarMovimiento(guardado, TipoMovimiento.BAJA, anterior,
                "Baja del inventario. Motivo: " + guardado.getMotivoBaja());

        return componer(guardado);
    }

    /** RF-43: reincorporacion al inventario operativo. */
    @Transactional
    public EquipoDto reincorporar(Long id, String motivo) {
        Equipo equipo = exigirEquipo(id);
        exigirResponsableDe(equipo);

        CondicionEquipo anterior = equipo.getCondicion();
        equipo.reincorporar();
        Equipo guardado = equipos.guardar(equipo);

        String detalle = motivo == null || motivo.isBlank()
                ? "Reincorporado al inventario."
                : "Reincorporado al inventario. Motivo: " + motivo.trim();
        registrarMovimiento(guardado, TipoMovimiento.REINCORPORACION, anterior, detalle);

        return componer(guardado);
    }

    // ------------------------------------------------------------------
    // RF-51: fotografia
    // ------------------------------------------------------------------

    @Transactional
    public EquipoDto asignarFoto(Long id, String nombreOriginal, String tipoContenido, InputStream contenido) {
        Equipo equipo = exigirEquipo(id);
        exigirQuienFotografia(equipo);

        String url = almacenFotos.guardar(nombreOriginal, tipoContenido, contenido);
        equipo.asignarFoto(url);
        Equipo guardado = equipos.guardar(equipo);

        return componer(guardado);
    }

    // ------------------------------------------------------------------
    // Autorizacion (RN-22, RN-23)
    // ------------------------------------------------------------------

    /**
     * RF-49: acota el filtro al ambito permitido.
     *
     * <p>Para un usuario operativo la Coordinacion se impone; para el
     * Administrador se respeta la que eligio, y si no eligio ninguna se
     * consulta toda la institucion en solo lectura (RF-46). Pedir el
     * inventario de una Coordinacion ajena no devuelve el propio: se
     * rechaza (RNF-10).</p>
     */
    private FiltroEquipos acotar(FiltroEquipos filtro) {
        UsuarioAutenticado actual = contexto.requerido();
        Long ambito = actual.ambitoDeConsulta(filtro.coordinacionId());
        return ambito == null ? filtro : filtro.enCoordinacion(ambito);
    }

    /** RF-46: el Administrador lee todo; los demas, solo lo suyo. */
    private void exigirLectura(Equipo equipo) {
        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.puedeLeerCoordinacion(equipo.getCoordinacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a los bienes de otra coordinacion.");
        }
    }

    /**
     * RN-22: el Administrador no escribe en el inventario. Devuelve al usuario
     * operativo (Responsable u Operador) que si puede hacerlo.
     */
    private UsuarioAutenticado exigirOperativo() {
        UsuarioAutenticado actual = contexto.requerido();
        if (actual.esAdmin()) {
            throw new AccesoDenegadoException(
                    "El Administrador no registra ni modifica bienes. Esa gestion corresponde al "
                            + "responsable y a los operadores de cada coordinacion.");
        }
        return actual;
    }

    private void exigirOperativoDe(Equipo equipo) {
        UsuarioAutenticado actual = exigirOperativo();
        actual.exigirAccesoA(equipo.getCoordinacionId());
    }

    /**
     * RF-51g: quien puede adjuntar la fotografia de un bien.
     *
     * <p>El <b>Responsable</b> siempre: sobre los bienes de su Coordinacion
     * escribe el, y la puede poner y sustituir cuantas veces quiera.</p>
     *
     * <p>El <b>Operador</b> solo la del alta: la del equipo que acaba de
     * registrar el mismo y mientras ese equipo no tenga ninguna. El alta y su
     * fotografia son dos llamadas seguidas porque el bien no puede recibir una
     * imagen antes de existir (RF-51d), no dos operaciones distintas; abrir
     * este endpoint al Operador sin las dos condiciones lo convertiria en la
     * escritura sobre un bien ajeno que RF-45 le niega —podria sustituir la
     * fotografia de cualquier equipo de la Coordinacion—.</p>
     */
    private void exigirQuienFotografia(Equipo equipo) {
        UsuarioAutenticado actual = exigirOperativo();
        actual.exigirAccesoA(equipo.getCoordinacionId());
        if (actual.esResponsable()) {
            return;
        }
        if (equipo.getFotoUrl() != null) {
            throw new AccesoDenegadoException(
                    "Este equipo ya tiene fotografia. Sustituirla corresponde al responsable de la "
                            + "coordinacion.");
        }
        if (equipo.getUsuarioRegistroId() == null
                || !equipo.getUsuarioRegistroId().equals(actual.id())) {
            throw new AccesoDenegadoException(
                    "Solo puede adjuntar la fotografia del equipo que usted mismo registro. En los "
                            + "demas equipos la pone el responsable de la coordinacion.");
        }
    }

    /** RF-45: editar, cambiar condicion y dar de baja son del Responsable. */
    private void exigirResponsableDe(Equipo equipo) {
        UsuarioAutenticado actual = exigirOperativo();
        if (!actual.esResponsable()) {
            throw new AccesoDenegadoException(
                    "Solo el responsable de la coordinacion puede modificar o dar de baja un bien.");
        }
        actual.exigirAccesoA(equipo.getCoordinacionId());
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private void registrarMovimiento(Equipo equipo, TipoMovimiento tipo,
                                     CondicionEquipo anterior, String detalle) {
        movimientos.registrar(MovimientoEquipo.registrar(
                equipo.getId(), tipo, anterior, equipo.getCondicion(), detalle,
                contexto.actual().orElse(null)));
    }

    private EquipoDto componer(Equipo equipo) {
        return EquipoDto.de(equipo,
                ubicaciones.nombreDeCoordinacion(equipo.getCoordinacionId()).orElse(null),
                ubicaciones.nombreDeLaboratorio(equipo.getLaboratorioId()).orElse(null),
                directorio.nombreDe(equipo.getResponsableId()).orElse("Sin responsable asignado"),
                nombreDeQuienLoTieneACargo(equipo),
                directorio.nombreDe(equipo.getUsuarioRegistroId()).orElse("Usuario no disponible"));
    }

    /**
     * RF-83: quien responde por el bien hoy.
     *
     * <p>El Operador a su cargo si lo tiene; si no, el Responsable vigente de
     * la Coordinacion, que es lo que significa no tenerlo (RN-37). Se resuelve
     * al leer y no se guarda: asi un relevo de Responsable no obliga a tocar
     * ni un bien, y la ficha nunca nombra a alguien que ya dejo el puesto.</p>
     */
    private String nombreDeQuienLoTieneACargo(Equipo equipo) {
        if (equipo.estaACargoDeUnOperador()) {
            return directorio.nombreDe(equipo.getResponsableEquipoId())
                    .orElse("Usuario no disponible");
        }
        return directorio.responsableVigenteDe(equipo.getCoordinacionId())
                .flatMap(directorio::nombreDe)
                .orElse("Sin responsable asignado");
    }

    private String nombreLaboratorioODefecto(Long laboratorioId) {
        if (laboratorioId == null) {
            return "ningun laboratorio";
        }
        return ubicaciones.nombreDeLaboratorio(laboratorioId).orElse("otro laboratorio");
    }

    private Equipo exigirEquipo(Long id) {
        return equipos.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el bien", id));
    }

    /** RN-14: unicidad institucional de serie y codigos. */
    private void validarUnicidad(NumeroSerie numeroSerie, CodigoBien inventario, CodigoBien patrimonial,
                                 Long idActual) {
        DatosInvalidosException errores = new DatosInvalidosException("Revise los datos ingresados.");

        // Los bienes sin serie comparten el valor convencional S/N.
        if (!numeroSerie.esSinSerie() && equipos.existeNumeroSerie(numeroSerie.valor(), idActual)) {
            errores.agregar("numeroSerie", "Ya existe un bien registrado con ese numero de serie.");
        }
        if (equipos.existeCodigoInventario(inventario.valor(), idActual)) {
            errores.agregar("codigoInventario", "Ya existe un bien con ese codigo de inventario.");
        }
        if (equipos.existeCodigoPatrimonial(patrimonial.valor(), idActual)) {
            errores.agregar("codigoPatrimonial", "Ya existe un bien con ese codigo patrimonial.");
        }
        if (errores.tieneErrores()) {
            throw errores;
        }
    }

    private ReferenciaCategoria resolverCategoria(Long categoriaId, ReferenciaCategoria actual) {
        if (categoriaId == null) {
            throw new DatosInvalidosException("categoriaId", "Seleccione la categoria del bien.");
        }
        Categoria categoria = categorias.buscarPorId(categoriaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la categoria", categoriaId));

        boolean yaLaTenia = actual != null && categoriaId.equals(actual.id());
        if (!categoria.isActiva() && !yaLaTenia) {
            throw new DatosInvalidosException("categoriaId", "La categoria seleccionada esta desactivada.");
        }
        return categoria.referencia();
    }

    /** RN-12: el laboratorio, si se indica, es de la misma Coordinacion. */
    private Long resolverLaboratorio(Long laboratorioId, Long coordinacionId) {
        if (laboratorioId == null) {
            return null;
        }
        if (!ubicaciones.laboratorioPerteneceA(laboratorioId, coordinacionId)) {
            throw new DatosInvalidosException("laboratorioId",
                    "El laboratorio seleccionado no existe, esta desactivado o pertenece a otra coordinacion.");
        }
        return laboratorioId;
    }
}
