package inictel.edu.pe.prestamos.application.dto;

import inictel.edu.pe.prestamos.domain.model.EstadoUso;
import inictel.edu.pe.prestamos.domain.model.UsoExterno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Vista de lectura de un uso externo (RF-78). */
public record UsoExternoDto(
        Long id,
        Long equipoId,
        Long coordinacionId,
        String equipoNombre,
        String equipoCodigoInventario,
        // 1
        String encargadoNombre,
        String encargadoCorreo,
        String encargadoCelular,
        // 2
        String estadoEquipoInicio,
        // 3
        String usuarioNombre,
        String usuarioCorreo,
        String usuarioTelefono,
        // 4
        String proyecto,
        // 5
        LocalDateTime fechaInicio,
        LocalDate fechaFinPrevista,
        LocalTime horaFinPrevista,
        String actividad,
        Long registradoPorId,
        String registradoPorNombre,
        // 6 a 10
        Boolean entregadoOperativo,
        Boolean devueltoOperativo,
        String incidente,
        String accionCorrectiva,
        String observaciones,
        LocalDateTime fechaCierre,
        String cerradoPorNombre,
        EstadoUso estado,
        String estadoEtiqueta) {

    public static UsoExternoDto de(UsoExterno u) {
        return new UsoExternoDto(
                u.getId(),
                u.getBien().id(),
                u.coordinacionId(),
                u.getBien().nombre(),
                u.getBien().codigoInventario(),
                u.getEncargadoNombre(),
                u.getEncargadoCorreo(),
                u.getEncargadoCelular(),
                u.getEstadoEquipoInicio(),
                u.getUsuarioNombre(),
                u.getUsuarioCorreo(),
                u.getUsuarioTelefono(),
                u.getProyecto(),
                u.getFechaInicio(),
                u.getFechaFinPrevista(),
                u.getHoraFinPrevista(),
                u.getActividad(),
                u.getRegistradoPor() != null ? u.getRegistradoPor().id() : null,
                u.getRegistradoPor() != null ? u.getRegistradoPor().nombreCompleto() : null,
                u.getEntregadoOperativo(),
                u.getDevueltoOperativo(),
                u.getIncidente(),
                u.getAccionCorrectiva(),
                u.getObservaciones(),
                u.getFechaCierre(),
                u.getCerradoPor() != null ? u.getCerradoPor().nombreCompleto() : null,
                u.getEstado(),
                u.getEstado().getEtiqueta());
    }
}
