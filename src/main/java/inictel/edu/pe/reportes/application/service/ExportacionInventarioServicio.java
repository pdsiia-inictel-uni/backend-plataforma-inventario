package inictel.edu.pe.reportes.application.service;

import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.inventario.application.dto.EquipoDto;
import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import inictel.edu.pe.inventario.domain.repository.FiltroEquipos;
import inictel.edu.pe.organizacion.application.service.ServicioPublicoOrganizacion;
import inictel.edu.pe.organizacion.domain.model.Institucion;
import inictel.edu.pe.reportes.application.dto.ArchivoExportadoDto;
import inictel.edu.pe.reportes.domain.model.FormatoExportacion;
import inictel.edu.pe.reportes.domain.service.GeneradorReporteInventario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Exportacion del inventario filtrado a Excel, CSV y PDF (RF-23).
 */
@Service
public class ExportacionInventarioServicio {

    private final ServicioPublicoInventario inventario;
    private final ServicioPublicoOrganizacion organizacion;
    private final GeneradorReporteInventario generador;
    private final ContextoUsuario contexto;

    public ExportacionInventarioServicio(ServicioPublicoInventario inventario,
                                         ServicioPublicoOrganizacion organizacion,
                                         GeneradorReporteInventario generador,
                                         ContextoUsuario contexto) {
        this.inventario = inventario;
        this.organizacion = organizacion;
        this.generador = generador;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public ArchivoExportadoDto exportar(FormatoExportacion formato, FiltroEquipos filtro) {
        // RN-23: el ambito lo decide el rol, no el parametro que llegue.
        // RNF-10: pedir la exportacion de otra coordinacion se rechaza.
        UsuarioAutenticado actual = contexto.requerido();
        Long ambito = actual.ambitoDeConsulta(filtro.coordinacionId());
        FiltroEquipos acotado = ambito == null ? filtro : filtro.enCoordinacion(ambito);

        List<EquipoDto> bienes = inventario.listarParaExportacion(acotado);
        GeneradorReporteInventario.Cabecera cabecera = cabeceraDe(ambito);

        byte[] contenido = switch (formato) {
            case EXCEL -> generador.aExcel(bienes, cabecera);
            case CSV -> generador.aCsv(bienes, cabecera);
            case PDF -> generador.aPdf(bienes, cabecera);
        };

        String nombre = "inventario-" + LocalDate.now() + "." + formato.getExtension();
        return new ArchivoExportadoDto(nombre, formato.getTipoContenido(), contenido);
    }

    /**
     * RF-52b: de quien es el reporte.
     *
     * <p>Los tres escalones de la jerarquia que el documento necesita nombrar:
     * la sede, que es constante (RN-01); y la Direccion y la Coordinacion del
     * ambito exportado. Cuando el Administrador exporta la institucion entera
     * no hay ninguna de las dos, y la cabecera lo dice con esas palabras en
     * lugar de dejar el hueco en blanco.</p>
     */
    private GeneradorReporteInventario.Cabecera cabeceraDe(Long coordinacionId) {
        if (coordinacionId == null) {
            return new GeneradorReporteInventario.Cabecera(Institucion.SEDE, null, null);
        }
        return organizacion.ubicacionDeCoordinacion(coordinacionId)
                .map(ubicacion -> new GeneradorReporteInventario.Cabecera(
                        Institucion.SEDE, ubicacion.direccion(), ubicacion.nombre()))
                .orElseGet(() -> new GeneradorReporteInventario.Cabecera(Institucion.SEDE, null, null));
    }
}
