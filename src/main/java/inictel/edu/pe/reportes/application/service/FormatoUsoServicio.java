package inictel.edu.pe.reportes.application.service;

import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.inventario.application.dto.EquipoDto;
import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import inictel.edu.pe.organizacion.application.service.ServicioPublicoOrganizacion;
import inictel.edu.pe.organizacion.domain.model.Institucion;
import inictel.edu.pe.prestamos.application.dto.UsoExternoDto;
import inictel.edu.pe.prestamos.application.service.ServicioPublicoPrestamos;
import inictel.edu.pe.reportes.application.dto.ArchivoExportadoDto;
import inictel.edu.pe.reportes.application.dto.FormatoUsoDto;
import inictel.edu.pe.reportes.domain.service.GeneradorFormatoUso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Formato de registro de uso de equipos de investigacion, en PDF (RF-78).
 *
 * <p>Se dibuja a partir de un registro de uso externo <b>guardado</b>: la
 * apertura (puntos 1 a 5), el cierre si ya lo tiene (6 a 10) y los datos del
 * bien (punto 2). Se puede imprimir en cualquier momento; completo, una vez
 * cerrado, para firmarse a mano (punto 9). Generarlo no escribe nada.</p>
 *
 * <p>Lo imprime quien puede ver el equipo: el Responsable y los Operadores de
 * su coordinacion, y el Administrador (RN-23).</p>
 */
@Service
public class FormatoUsoServicio {

    /** Punto 1 del formato: el responsable del equipamiento es el Coordinador. */
    private static final String RESPONSABLE_EQUIPAMIENTO = "Coordinador";

    private final ServicioPublicoPrestamos prestamos;
    private final ServicioPublicoInventario inventario;
    private final ServicioPublicoOrganizacion organizacion;
    private final GeneradorFormatoUso generador;
    private final ContextoUsuario contexto;

    public FormatoUsoServicio(ServicioPublicoPrestamos prestamos,
                              ServicioPublicoInventario inventario,
                              ServicioPublicoOrganizacion organizacion,
                              GeneradorFormatoUso generador,
                              ContextoUsuario contexto) {
        this.prestamos = prestamos;
        this.inventario = inventario;
        this.organizacion = organizacion;
        this.generador = generador;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public ArchivoExportadoDto generar(Long usoId) {
        UsoExternoDto uso = prestamos.usoExterno(usoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("el registro de uso", usoId));

        if (!contexto.requerido().puedeLeerCoordinacion(uso.coordinacionId())) {
            throw new AccesoDenegadoException("No tiene acceso a los equipos de otra coordinación.");
        }

        EquipoDto equipo = inventario.fichaDe(uso.equipoId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("el bien", uso.equipoId()));

        boolean cerrado = uso.fechaCierre() != null;

        FormatoUsoDto formato = new FormatoUsoDto(
                uso.id(),
                Institucion.SEDE,
                organizacion.ubicacionDeCoordinacion(equipo.coordinacionId())
                        .map(ServicioPublicoOrganizacion.CoordinacionUbicada::direccion)
                        .orElse(null),
                equipo.coordinacion(),
                equipo.laboratorio(),

                RESPONSABLE_EQUIPAMIENTO,
                uso.encargadoNombre(),
                uso.encargadoCorreo(),
                uso.encargadoCelular(),

                equipo.nombre(),
                equipo.codigoPatrimonial(),
                equipo.codigoInventario(),
                equipo.marca(),
                equipo.modelo(),
                equipo.numeroSerie(),
                equipo.costo(),
                equipo.fechaAdquisicion(),
                equipo.coordinacion(),
                // La condicion con que el equipo salio, guardada al abrir el uso.
                uso.estadoEquipoInicio(),

                uso.usuarioNombre(),
                uso.usuarioCorreo(),
                uso.usuarioTelefono(),

                uso.proyecto(),

                uso.fechaInicio(),
                cerrado ? uso.fechaCierre().toLocalDate() : uso.fechaFinPrevista(),
                cerrado ? uso.fechaCierre().toLocalTime() : uso.horaFinPrevista(),
                uso.actividad(),

                uso.entregadoOperativo(),
                uso.devueltoOperativo(),

                uso.incidente(),
                uso.accionCorrectiva(),

                uso.observaciones(),
                cerrado);

        return new ArchivoExportadoDto(nombreArchivo(uso, equipo), "application/pdf",
                generador.generar(formato));
    }

    private String nombreArchivo(UsoExternoDto uso, EquipoDto equipo) {
        String codigo = equipo.codigoInventario() == null ? String.valueOf(equipo.id())
                : equipo.codigoInventario().replaceAll("[^A-Za-z0-9._-]", "-");
        return "registro-uso-" + uso.id() + "-" + codigo + ".pdf";
    }
}
