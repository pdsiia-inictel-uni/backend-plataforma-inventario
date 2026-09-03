package inictel.edu.pe.reportes.infrastructure.exportacion;

import inictel.edu.pe.reportes.application.dto.FormatoUsoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Formato de registro de uso en PDF (RF-78).
 *
 * <p>El documento se dibuja con la mitad de sus campos en blanco casi siempre
 * —el formato se termina de rellenar a mano—, de modo que lo que hay que
 * comprobar es justamente que un formato vacio sale igual de bien que uno
 * lleno: una celda sin texto, una fecha ausente o una casilla sin marcar no
 * pueden romper la generacion.</p>
 */
class GeneradorFormatoUsoPdfTest {

    private final GeneradorFormatoUsoPdf generador = new GeneradorFormatoUsoPdf();

    private FormatoUsoDto formato(String investigador,
                                  LocalDate fechaFin,
                                  LocalTime horaFin,
                                  Boolean entregado,
                                  Boolean devuelto,
                                  BigDecimal valor) {
        return new FormatoUsoDto(
                "Sede Central INICTEL-UNI",
                "Direccion de Investigacion y Desarrollo Tecnologico",
                "Coordinacion de Investigacion",
                "Laboratorio de Procesamiento Digital de Senales",
                "Coordinador",
                investigador,
                investigador == null ? null : "encargado@inictel-uni.edu.pe",
                investigador == null ? null : "999888777",
                "Espectrografo RESONON",
                "PAT-000123",
                "INV-2024-0102",
                "Resonon",
                "PIKA IR+",
                "SN-778899",
                valor,
                LocalDate.of(2024, 11, 18),
                "Teledeteccion e IA",
                "Operativo",
                investigador,
                null,
                null,
                null,
                LocalDateTime.of(2026, 9, 2, 10, 30),
                fechaFin,
                horaFin,
                null,
                entregado,
                devuelto,
                null,
                null,
                "Ana Torres Quispe",
                null);
    }

    private void esUnPdf(byte[] documento) {
        assertNotNull(documento);
        assertTrue(documento.length > 1000, "el PDF llego vacio o truncado");
        String cabecera = new String(documento, 0, 5, StandardCharsets.ISO_8859_1);
        assertTrue(cabecera.startsWith("%PDF-"), "no es un PDF: " + cabecera);
    }

    @Test
    @DisplayName("El formato se genera con todos los campos manuales en blanco")
    void generaConCamposVacios() {
        esUnPdf(generador.generar(formato(null, null, null, null, null, new BigDecimal("477001.00"))));
    }

    @Test
    @DisplayName("El formato se genera con los campos manuales rellenos")
    void generaConCamposRellenos() {
        esUnPdf(generador.generar(formato(
                "Luis Fernando Rojas Vega",
                LocalDate.of(2026, 9, 5),
                LocalTime.of(17, 0),
                Boolean.TRUE,
                Boolean.FALSE,
                new BigDecimal("477001.00"))));
    }

    @Test
    @DisplayName("Un bien sin costo registrado no rompe el documento")
    void generaSinValorDeCompra() {
        esUnPdf(generador.generar(formato("Luis Rojas", null, null, Boolean.TRUE, null, null)));
    }
}
