package inictel.edu.pe.reportes.infrastructure.exportacion;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import inictel.edu.pe.reportes.application.dto.FormatoUsoDto;
import inictel.edu.pe.reportes.domain.service.GeneradorFormatoUso;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Dibuja el formato de registro de uso de equipos de investigacion (RF-78).
 *
 * <p>Reproduce el formulario que el laboratorio ya usaba en papel, con sus
 * diez puntos y en el mismo orden, para que quien lo firma reconozca el
 * documento. Lo que el sistema sabe del bien viene impreso; lo que no, viene
 * como un recuadro en blanco de la altura suficiente para escribirlo a mano,
 * porque el formato se imprime y se firma.</p>
 *
 * <p>El documento no se guarda en ninguna parte: se devuelve al navegador y
 * ahi termina (RN-36).</p>
 */
@Component
public class GeneradorFormatoUsoPdf implements GeneradorFormatoUso {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Blanco y negro (v3.10).
     *
     * <p>Es un formato que se imprime, se firma a mano y se archiva. En una
     * impresora comun el color no aporta jerarquia: la aportan el tamano, la
     * negrita y las lineas. Las bandas de seccion, que antes iban en azul con
     * texto blanco, pasan a texto negro sobre blanco con una linea inferior
     * gruesa, que se lee igual de bien y no gasta toner en un documento que se
     * imprime cada vez que sale un equipo.</p>
     */
    private static final Color LINEA = Color.BLACK;

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.5f, Color.BLACK);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, Color.BLACK);
    private static final Font AMBITO = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8.5f, Color.BLACK);
    private static final Font SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, Color.BLACK);
    private static final Font ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.BLACK);
    private static final Font VALOR = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
    private static final Font NOTA = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7.5f, Color.BLACK);
    private static final Font PIE = FontFactory.getFont(FontFactory.HELVETICA, 7f, Color.BLACK);

    @Override
    public byte[] generar(FormatoUsoDto f) {
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 40, 40, 34, 34);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            cabecera(documento, f);

            // 1. Responsable del equipamiento: el cargo es fijo, las personas no.
            documento.add(seccion("1.  Responsable del Equipamiento: " + texto(f.responsableEquipamiento())));
            documento.add(datos(
                    campo("Investigador encargado", f.investigadorEncargado()),
                    campo("Correo", f.correoEncargado()),
                    campo("Celular", f.celularEncargado())));

            // 2. Identificacion del equipo: todo lo pone el sistema.
            documento.add(seccion("2.  Identificación del Equipo"));
            documento.add(datos(
                    campo("Descripción (nombre del equipo)", f.descripcion()),
                    campo("Código patrimonial", f.codigoPatrimonial()),
                    campo("Código de inventario", f.codigoInventario()),
                    campo("Marca", f.marca()),
                    campo("Modelo", f.modelo()),
                    campo("Número de serie", f.numeroSerie()),
                    campo("Valor de compra (soles)", importe(f.valorCompra())),
                    campo("Fecha de adquisición", fecha(f.fechaAdquisicion())),
                    campo("Área o línea de investigación", f.areaInvestigacion()),
                    campo("Estado del equipo", f.estadoEquipo())));

            // 3. Datos del usuario.
            documento.add(seccion("3.  Datos del Usuario"));
            documento.add(datos(
                    campo("Nombre del investigador", f.nombreInvestigador()),
                    campo("Correo institucional", f.correoInvestigador()),
                    campo("Teléfono de contacto", f.telefonoInvestigador())));

            // 4. Proyecto asociado.
            documento.add(seccion("4.  Proyecto de investigación asociado y actividad realizada "
                    + "(PAO – Prociencia u otro)"));
            documento.add(caja(f.proyecto(), 3));

            // 5. Registro de uso: el inicio lo pone el reloj del servidor.
            documento.add(seccion("5.  Registro de Uso del Equipo"));
            documento.add(datos(
                    campo("Fecha de inicio del uso", fecha(f.inicioUso().toLocalDate())),
                    campo("Hora", hora(f.inicioUso().toLocalTime())),
                    campo("Fecha de fin del uso", fecha(f.fechaFinUso())),
                    campo("Hora", hora(f.horaFinUso()))));
            documento.add(rotulo("Actividad realizada:"));
            documento.add(caja(f.actividadRealizada(), 3));

            // 6 y 7. Conformidad de entrega y de devolucion.
            documento.add(seccion("6.  ¿El equipo fue entregado en condiciones operativas?"));
            documento.add(casillas(f.entregadoOperativo()));

            documento.add(seccion("7.  ¿El equipo fue devuelto en condiciones operativas?"));
            documento.add(casillas(f.devueltoOperativo()));

            // 8. Incidentes.
            documento.add(seccion("8.  Incidentes, fallas o daños (si aplica)"));
            documento.add(nota("Indicar la fecha, la hora y el nombre de la persona a quien se reportó."));
            documento.add(rotulo("Descripción del incidente o falla:"));
            documento.add(caja(f.incidente(), 3));
            documento.add(rotulo("Acción correctiva realizada:"));
            documento.add(caja(f.accionCorrectiva(), 3));

            // 9. Firmas.
            documento.add(seccion("9.  Autorizaciones y Firmas"));
            documento.add(firmas(f.coordinadorQueFirma()));

            // 10. Observaciones generales.
            documento.add(seccion("10.  Observaciones Generales"));
            documento.add(caja(f.observaciones(), 3));

            documento.add(pie());

            documento.close();
            return salida.toByteArray();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No se pudo generar el formato de registro de uso: " + ex.getMessage(), ex);
        }
    }

    // ------------------------------------------------------------------
    // Piezas del documento
    // ------------------------------------------------------------------

    private void cabecera(Document documento, FormatoUsoDto f) {
        Paragraph titulo = new Paragraph("FORMATO DE REGISTRO DE USO DE EQUIPOS DE INVESTIGACIÓN", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph institucion = new Paragraph(
                "Instituto Nacional de Investigación y Capacitación de Telecomunicaciones", SUBTITULO);
        institucion.setAlignment(Element.ALIGN_CENTER);
        documento.add(institucion);

        Paragraph sede = new Paragraph(texto(f.sede()), SUBTITULO);
        sede.setAlignment(Element.ALIGN_CENTER);
        documento.add(sede);

        // La dependencia del bien, de mayor a menor: Direccion, Coordinacion y
        // laboratorio. En el papel era una linea fija con el nombre de un solo
        // laboratorio, porque el formato se habia hecho para uno.
        String ambito = java.util.stream.Stream.of(f.direccion(), f.coordinacion(), f.laboratorio())
                .map(this::texto)
                .filter(parte -> !parte.isEmpty())
                .reduce((a, b) -> a + "  -  " + b)
                .orElse("");
        Paragraph donde = new Paragraph(ambito, AMBITO);
        donde.setAlignment(Element.ALIGN_CENTER);
        donde.setSpacingAfter(10f);
        documento.add(donde);
    }

    /** Banda de titulo de cada uno de los diez puntos del formato. */
    private PdfPTable seccion(String titulo) {
        PdfPTable banda = new PdfPTable(1);
        banda.setWidthPercentage(100);
        banda.setSpacingBefore(8f);
        banda.setSpacingAfter(3f);

        PdfPCell celda = new PdfPCell(new Phrase(titulo, SECCION));
        // Sin relleno: la banda se reconoce por la negrita y por su linea
        // inferior gruesa, que es lo que separa una seccion de la siguiente.
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(LINEA);
        celda.setBorderWidthBottom(1.2f);
        celda.setPaddingTop(3f);
        celda.setPaddingBottom(3f);
        celda.setPaddingLeft(0f);
        banda.addCell(celda);
        return banda;
    }

    /** Un par etiqueta/valor. El valor vacio queda como celda para escribir. */
    private record Campo(String etiqueta, String valor) {
    }

    private Campo campo(String etiqueta, String valor) {
        return new Campo(etiqueta, valor);
    }

    private PdfPTable datos(Campo... campos) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{34f, 66f});

        for (Campo campo : campos) {
            PdfPCell etiqueta = new PdfPCell(new Phrase(campo.etiqueta(), ETIQUETA));
            etiqueta.setBorderColor(LINEA);
            etiqueta.setBorderWidth(0.4f);
            etiqueta.setPadding(4.5f);
            tabla.addCell(etiqueta);

            PdfPCell valor = new PdfPCell(new Phrase(texto(campo.valor()), VALOR));
            valor.setBorderColor(LINEA);
            valor.setBorderWidth(0.4f);
            valor.setPadding(4.5f);
            // Alto minimo: el campo vacio tiene que dar sitio para escribirlo.
            valor.setMinimumHeight(16f);
            tabla.addCell(valor);
        }
        return tabla;
    }

    /** Recuadro de texto largo; si viene vacio, deja el espacio en blanco. */
    private PdfPTable caja(String contenido, int renglones) {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);

        PdfPCell celda = new PdfPCell(new Phrase(texto(contenido), VALOR));
        celda.setBorderColor(LINEA);
        celda.setBorderWidth(0.4f);
        celda.setPadding(5f);
        celda.setMinimumHeight(renglones * 13f);
        tabla.addCell(celda);
        return tabla;
    }

    private Paragraph rotulo(String texto) {
        Paragraph parrafo = new Paragraph(texto, ETIQUETA);
        parrafo.setSpacingBefore(5f);
        parrafo.setSpacingAfter(2f);
        return parrafo;
    }

    private Paragraph nota(String texto) {
        Paragraph parrafo = new Paragraph(texto, NOTA);
        parrafo.setSpacingAfter(2f);
        return parrafo;
    }

    /**
     * Las dos casillas de una pregunta de si o no.
     *
     * <p>Se marca la que corresponda y la otra queda en blanco. Si nadie
     * respondio, las dos quedan vacias: es lo que hace el papel, y el punto 7
     * se responde casi siempre despues, cuando el equipo vuelve.</p>
     */
    private PdfPTable casillas(Boolean respuesta) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(40);
        tabla.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabla.setWidths(new float[]{1f, 1f});

        tabla.addCell(casilla("Sí", Boolean.TRUE.equals(respuesta)));
        tabla.addCell(casilla("No", Boolean.FALSE.equals(respuesta)));
        return tabla;
    }

    private PdfPCell casilla(String etiqueta, boolean marcada) {
        PdfPCell celda = new PdfPCell(new Phrase(etiqueta + "   [ " + (marcada ? "X" : " ") + " ]", VALOR));
        celda.setBorderColor(LINEA);
        celda.setBorderWidth(0.4f);
        celda.setPadding(5f);
        return celda;
    }

    /**
     * Punto 9: las dos firmas.
     *
     * <p>La del coordinador lleva impreso el nombre de quien emite el
     * documento —es quien responde por el equipo— y la del investigador queda
     * en blanco, porque la escribe quien se lo lleva.</p>
     */
    private PdfPTable firmas(String coordinador) throws Exception {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1f, 1f});
        tabla.setSpacingBefore(6f);

        tabla.addCell(bloqueFirma("Firma del coordinador", texto(coordinador)));
        tabla.addCell(bloqueFirma("Firma del investigador", ""));
        return tabla;
    }

    private PdfPCell bloqueFirma(String rotulo, String nombre) {
        Paragraph contenido = new Paragraph();
        contenido.add(new Phrase("\n\n\n", VALOR));
        contenido.add(new Phrase("____________________________\n", VALOR));
        contenido.add(new Phrase(rotulo + "\n", ETIQUETA));
        contenido.add(new Phrase((nombre.isBlank() ? " " : nombre) + "\n", VALOR));
        contenido.add(new Phrase("Fecha:  ____ / ____ / ________", VALOR));

        PdfPCell celda = new PdfPCell(contenido);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(6f);
        return celda;
    }

    /**
     * Pie del documento.
     *
     * <p>Dice lo que este papel no es: el formato acompania la salida fisica
     * del equipo, y el prestamo se registra aparte en el sistema (RF-79).</p>
     */
    private Paragraph pie() {
        LocalDateTime ahora = LocalDateTime.now();
        Paragraph parrafo = new Paragraph(
                "Documento generado por el Sistema de Gestión de Inventarios de INICTEL-UNI el "
                        + ahora.format(FECHA) + " a las " + ahora.format(HORA)
                        + ".  No sustituye al registro del préstamo en el sistema.", PIE);
        parrafo.setAlignment(Element.ALIGN_CENTER);
        parrafo.setSpacingBefore(10f);
        return parrafo;
    }

    // ------------------------------------------------------------------
    // Formato de los valores
    // ------------------------------------------------------------------

    /** Lo que no se sabe queda en blanco: es un hueco para escribir a mano. */
    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "" : valor.trim();
    }

    private String importe(BigDecimal valor) {
        return valor == null ? "" : "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String fecha(LocalDate valor) {
        return valor == null ? "" : valor.format(FECHA);
    }

    private String hora(LocalTime valor) {
        return valor == null ? "" : valor.format(HORA);
    }
}
