package inictel.edu.pe.prestamos.application.dto;

import inictel.edu.pe.prestamos.domain.model.EstadoPrestamo;
import inictel.edu.pe.prestamos.domain.model.Prestamo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Vista de lectura de un prestamo (RF-66, RF-67).
 */
public record PrestamoDto(
        Long id,
        Long coordinacionId,
        Long equipoId,
        String equipoNombre,
        String equipoCodigoInventario,
        String equipoNumeroSerie,
        String nombrePersona,
        String dniPersona,
        String destino,
        LocalDateTime fechaPrestamo,
        LocalDate fechaEstimadaDevolucion,
        String observacionesSalida,
        LocalDateTime fechaDevolucion,
        Boolean conforme,
        boolean reportaDano,
        String observacionesRetorno,
        Long usuarioPrestaId,
        String usuarioPrestaNombre,
        Long usuarioRecibeId,
        String usuarioRecibeNombre,
        EstadoPrestamo estado,
        String estadoEtiqueta,
        boolean vencido,
        long diasAtraso) {

    public static PrestamoDto de(Prestamo p) {
        return new PrestamoDto(
                p.getId(),
                p.coordinacionId(),
                p.getBien().id(),
                p.getBien().nombre(),
                p.getBien().codigoInventario(),
                p.getBien().numeroSerie(),
                p.getPersona().nombre(),
                p.getPersona().dni(),
                p.getDestino(),
                p.getFechaPrestamo(),
                p.getFechaEstimadaDevolucion(),
                p.getObservacionesSalida(),
                p.getFechaDevolucion(),
                p.getConforme(),
                p.isReportaDano(),
                p.getObservacionesRetorno(),
                p.getUsuarioPresta() != null ? p.getUsuarioPresta().id() : null,
                p.getUsuarioPresta() != null ? p.getUsuarioPresta().nombreCompleto() : null,
                p.getUsuarioRecibe() != null ? p.getUsuarioRecibe().id() : null,
                p.getUsuarioRecibe() != null ? p.getUsuarioRecibe().nombreCompleto() : null,
                p.getEstado(),
                p.getEstado().getEtiqueta(),
                p.estaVencido(),
                p.diasAtraso());
    }
}
