package inictel.edu.pe.reportes.application.service;

import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.inventario.application.dto.EquipoDto;
import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import inictel.edu.pe.organizacion.application.service.ServicioPublicoOrganizacion;
import inictel.edu.pe.organizacion.domain.model.Institucion;
import inictel.edu.pe.reportes.application.comando.GenerarFormatoUsoComando;
import inictel.edu.pe.reportes.application.dto.ArchivoExportadoDto;
import inictel.edu.pe.reportes.application.dto.FormatoUsoDto;
import inictel.edu.pe.reportes.domain.service.GeneradorFormatoUso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Formato de registro de uso de equipos de investigacion (RF-78, RF-79).
 *
 * <p>Es el papel que el laboratorio hace firmar cuando un equipo sale a
 * trabajar: quien lo entrega, quien lo usa, en que proyecto, desde cuando y
 * en que estado fue y volvio. El sistema lo rellena con lo que ya sabe del
 * bien y deja en blanco lo que solo saben las personas.</p>
 *
 * <p><b>Genera un documento y nada mas (RN-36).</b> No escribe ninguna fila:
 * ni el formato, ni un movimiento en el historial del bien, ni un prestamo.
 * Tampoco cambia la condicion del equipo, que sigue como estaba —Operativo,
 * si lo estaba— hasta que alguien registre la salida de verdad (RF-59). El
 * documento es un tramite del laboratorio; el prestamo es el registro del
 * sistema, y son dos cosas distintas que la pantalla se encarga de recordar
 * (RF-79).</p>
 *
 * <p>Solo lo genera el <b>Responsable</b>, y solo sobre bienes de su
 * Coordinacion: es quien firma el documento como coordinador (punto 9) y
 * quien responde por el equipo que sale (RF-78, RN-23).</p>
 */
@Service
public class FormatoUsoServicio {

    /** Punto 1 del formato: el responsable del equipamiento es el Coordinador. */
    private static final String RESPONSABLE_EQUIPAMIENTO = "Coordinador";

    private final ServicioPublicoInventario inventario;
    private final ServicioPublicoOrganizacion organizacion;
    private final GeneradorFormatoUso generador;
    private final ContextoUsuario contexto;

    public FormatoUsoServicio(ServicioPublicoInventario inventario,
                              ServicioPublicoOrganizacion organizacion,
                              GeneradorFormatoUso generador,
                              ContextoUsuario contexto) {
        this.inventario = inventario;
        this.organizacion = organizacion;
        this.generador = generador;
        this.contexto = contexto;
    }

    /**
     * @return el PDF y el nombre con el que se descarga; nada queda guardado
     */
    @Transactional(readOnly = true)
    public ArchivoExportadoDto generar(Long equipoId, GenerarFormatoUsoComando datos) {
        UsuarioAutenticado actual = contexto.requerido();
        if (!actual.esResponsable()) {
            throw new AccesoDenegadoException(
                    "El formato de registro de uso lo emite el responsable de la coordinacion.");
        }

        EquipoDto equipo = inventario.fichaDe(equipoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el bien", equipoId));

        // RN-23: un responsable no emite documentos sobre bienes ajenos.
        actual.exigirAccesoA(equipo.coordinacionId());

        FormatoUsoDto formato = new FormatoUsoDto(
                Institucion.SEDE,
                // RF-52b: el documento nombra la dependencia entera, de mayor a
                // menor. Un papel que se firma y se archiva tiene que decir a
                // que Direccion pertenece el equipo que sale.
                organizacion.ubicacionDeCoordinacion(equipo.coordinacionId())
                        .map(ServicioPublicoOrganizacion.CoordinacionUbicada::direccion)
                        .orElse(null),
                equipo.coordinacion(),
                equipo.laboratorio(),

                RESPONSABLE_EQUIPAMIENTO,
                datos.investigadorEncargado(),
                datos.correoEncargado(),
                datos.celularEncargado(),

                equipo.nombre(),
                equipo.codigoPatrimonial(),
                equipo.codigoInventario(),
                equipo.marca(),
                equipo.modelo(),
                equipo.numeroSerie(),
                equipo.costo(),
                equipo.fechaAdquisicion(),
                equipo.coordinacion(),
                // La condicion que el bien tiene ahora mismo, no la que se
                // suponga: si esta prestado o en mantenimiento, el documento lo
                // dice y quien lo firma se entera antes de llevarselo.
                equipo.condicionEtiqueta(),

                datos.nombreInvestigador(),
                datos.correoInvestigador(),
                datos.telefonoInvestigador(),

                datos.proyecto(),

                // RF-78: el uso empieza cuando se emite el documento, con la
                // hora del servidor. El fin lo escribe quien lo firma.
                LocalDateTime.now(),
                datos.fechaFinUso(),
                datos.horaFinUso(),
                datos.actividadRealizada(),

                datos.entregadoOperativo(),
                datos.devueltoOperativo(),

                datos.incidente(),
                datos.accionCorrectiva(),

                actual.nombreCompleto(),

                datos.observaciones());

        return new ArchivoExportadoDto(nombreArchivo(equipo), "application/pdf",
                generador.generar(formato));
    }

    private String nombreArchivo(EquipoDto equipo) {
        String codigo = equipo.codigoInventario() == null ? String.valueOf(equipo.id())
                : equipo.codigoInventario().replaceAll("[^A-Za-z0-9._-]", "-");
        return "registro-uso-" + codigo + "-" + LocalDate.now() + ".pdf";
    }
}
