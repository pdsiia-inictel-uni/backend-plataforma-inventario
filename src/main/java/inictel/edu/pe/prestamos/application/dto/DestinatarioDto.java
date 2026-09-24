package inictel.edu.pe.prestamos.application.dto;

import inictel.edu.pe.prestamos.domain.model.Destinatario;

/**
 * Persona a la que puede entregarse un equipo (RF-59). No lleva el DNI: para
 * elegirla basta el nombre y el rol, y el dato personal no viaja a quien no lo
 * necesita (RNF-46).
 */
public record DestinatarioDto(Long id, String nombreCompleto, String rolEtiqueta, Long coordinacionId) {

    public static DestinatarioDto de(Destinatario d) {
        return new DestinatarioDto(d.id(), d.nombreCompleto(), d.rolEtiqueta(), d.coordinacionId());
    }
}
