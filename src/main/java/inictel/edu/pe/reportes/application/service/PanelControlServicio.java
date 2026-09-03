package inictel.edu.pe.reportes.application.service;

import inictel.edu.pe.compartido.domain.seguridad.ContextoUsuario;
import inictel.edu.pe.compartido.domain.seguridad.UsuarioAutenticado;
import inictel.edu.pe.iam.application.service.ServicioPublicoIam;
import inictel.edu.pe.inventario.application.dto.ResumenBienesDto;
import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import inictel.edu.pe.organizacion.application.dto.CoordinacionDto;
import inictel.edu.pe.organizacion.application.dto.EstructuraDto;
import inictel.edu.pe.organizacion.application.service.GestionOrganizacionServicio;
import inictel.edu.pe.organizacion.application.service.ServicioPublicoOrganizacion;
import inictel.edu.pe.organizacion.domain.model.Institucion;
import inictel.edu.pe.prestamos.application.service.ServicioPublicoPrestamos;
import inictel.edu.pe.reportes.application.dto.PanelControlDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Indicadores del panel de control (RF-75 .. RF-77).
 *
 * <p>Reportes no tiene persistencia propia: es un contexto de composicion que
 * consulta a los demas a traves de sus servicios publicos.</p>
 *
 * <p>El panel se arma segun el rol de quien lo pide, no segun lo que pida el
 * cliente: el Administrador recibe cifras institucionales; el Responsable y el
 * Operador, las de su Coordinacion (RN-23).</p>
 */
@Service
public class PanelControlServicio {

    private final ServicioPublicoInventario inventario;
    private final ServicioPublicoPrestamos prestamos;
    private final ServicioPublicoIam iam;
    private final ServicioPublicoOrganizacion organizacion;
    private final GestionOrganizacionServicio estructura;
    private final ContextoUsuario contexto;

    public PanelControlServicio(ServicioPublicoInventario inventario,
                                ServicioPublicoPrestamos prestamos,
                                ServicioPublicoIam iam,
                                ServicioPublicoOrganizacion organizacion,
                                GestionOrganizacionServicio estructura,
                                ContextoUsuario contexto) {
        this.inventario = inventario;
        this.prestamos = prestamos;
        this.iam = iam;
        this.organizacion = organizacion;
        this.estructura = estructura;
        this.contexto = contexto;
    }

    @Transactional(readOnly = true)
    public PanelControlDto resumen() {
        UsuarioAutenticado actual = contexto.requerido();
        return actual.esAdmin() ? panelInstitucional(actual) : panelDeCoordinacion(actual);
    }

    /** RF-75: vision de toda la institucion, con la alerta de coordinaciones huerfanas. */
    private PanelControlDto panelInstitucional(UsuarioAutenticado actual) {
        ResumenBienesDto bienes = inventario.resumen(null);
        EstructuraDto arbol = estructura.estructura(false);

        long coordinaciones = arbol.direcciones().stream()
                .mapToLong(rama -> rama.coordinaciones().size())
                .sum();
        long laboratorios = arbol.direcciones().stream()
                .flatMap(rama -> rama.coordinaciones().stream())
                .mapToLong(CoordinacionDto::laboratorios)
                .sum();

        return new PanelControlDto(
                actual.rol(),
                Institucion.NOMBRE,
                null,
                bienes.total(),
                bienes.operativos(),
                bienes.prestados(),
                bienes.enMantenimiento(),
                bienes.dadosDeBaja(),
                bienes.revisionPendiente(),
                prestamos.contarActivos(null),
                prestamos.contarVencidos(null),
                iam.contarUsuariosActivos(),
                arbol.direcciones().size(),
                coordinaciones,
                laboratorios,
                arbol.coordinacionesSinResponsable(),
                conteos(bienes),
                condiciones(bienes));
    }

    /** RF-76, RF-77: vision acotada a la Coordinacion del usuario. */
    private PanelControlDto panelDeCoordinacion(UsuarioAutenticado actual) {
        Long coordinacionId = actual.coordinacionRequerida();
        ResumenBienesDto bienes = inventario.resumen(coordinacionId);
        String ambito = organizacion.nombreCompletoDeCoordinacion(coordinacionId)
                .orElse("Su coordinacion");

        return new PanelControlDto(
                actual.rol(),
                ambito,
                coordinacionId,
                bienes.total(),
                bienes.operativos(),
                bienes.prestados(),
                bienes.enMantenimiento(),
                bienes.dadosDeBaja(),
                bienes.revisionPendiente(),
                prestamos.contarActivos(coordinacionId),
                prestamos.contarVencidos(coordinacionId),
                iam.contarUsuariosActivosEn(coordinacionId),
                0, 0, 0,
                // RN-07: 1 si la propia coordinacion esta sin responsable, y
                // por tanto parada. Es la unica alerta que le impide trabajar a
                // un Operador, asi que su panel la enuncia antes de ofrecerle
                // los dos botones que no van a funcionar (RNF-22).
                iam.responsableDe(coordinacionId).isPresent() ? 0 : 1,
                conteos(bienes),
                condiciones(bienes));
    }

    private List<PanelControlDto.ConteoDto> conteos(ResumenBienesDto bienes) {
        return bienes.porCategoria().entrySet().stream()
                .map(e -> new PanelControlDto.ConteoDto(e.getKey(), e.getValue()))
                .toList();
    }

    /** Orden fijo y estable, para que las barras no bailen entre recargas. */
    private List<PanelControlDto.ConteoDto> condiciones(ResumenBienesDto bienes) {
        return List.of(
                new PanelControlDto.ConteoDto("Operativos", bienes.operativos()),
                new PanelControlDto.ConteoDto("Prestados", bienes.prestados()),
                new PanelControlDto.ConteoDto("En mantenimiento", bienes.enMantenimiento()),
                new PanelControlDto.ConteoDto("Dados de baja", bienes.dadosDeBaja()));
    }
}
