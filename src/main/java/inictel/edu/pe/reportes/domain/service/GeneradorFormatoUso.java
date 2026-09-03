package inictel.edu.pe.reportes.domain.service;

import inictel.edu.pe.reportes.application.dto.FormatoUsoDto;

/**
 * Puerto de salida: materializa el formato de registro de uso en un PDF
 * (RF-78).
 *
 * <p>Se declara aqui, en el dominio, y lo implementa la infraestructura, que
 * es la unica que conoce la biblioteca de PDF. El mismo motivo por el que
 * {@link GeneradorReporteInventario} vive al lado: cambiar de biblioteca no
 * debe obligar a tocar el servicio que decide que dice el documento.</p>
 */
public interface GeneradorFormatoUso {

    /** @return el PDF completo, sin guardarse en ninguna parte */
    byte[] generar(FormatoUsoDto formato);
}
