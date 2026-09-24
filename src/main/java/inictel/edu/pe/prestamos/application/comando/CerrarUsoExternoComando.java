package inictel.edu.pe.prestamos.application.comando;

/**
 * Parte final del registro de uso externo: puntos 6 a 10 del formato (RF-78).
 * El punto 9, las firmas, se pone a mano sobre el documento impreso.
 */
public record CerrarUsoExternoComando(
        Boolean entregadoOperativo,
        Boolean devueltoOperativo,
        String incidente,
        String accionCorrectiva,
        String observaciones) {
}
