package inictel.edu.pe.reportes.infrastructure.exportacion;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import inictel.edu.pe.inventario.application.dto.EquipoDto;
import inictel.edu.pe.reportes.domain.service.GeneradorReporteInventario;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador que materializa el inventario filtrado en Excel, CSV y PDF (RF-23).
 */
@Component
public class GeneradorReporteInventarioArchivos implements GeneradorReporteInventario {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter SOLO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] CABECERAS = {
            "N°", "Coordinacion", "Laboratorio", "Nombre", "Marca", "Modelo", "N° Serie",
            "Cod. Inventario", "Cod. Patrimonial", "Categoria", "Condicion",
            "Fecha adq.", "Costo (S/)", "Responsable del equipo", "Fecha de registro",
            "Registrado por", "Motivo de baja"
    };

    // ------------------------------------------------------------------
    // Excel (.xlsx)
    // ------------------------------------------------------------------

    @Override
    public byte[] aExcel(List<EquipoDto> bienes, Cabecera cabecera) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet hoja = libro.createSheet("Inventario");

            CellStyle estiloTitulo = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            estiloTitulo.setFont(fuenteTitulo);

            CellStyle estiloAmbito = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteAmbito = libro.createFont();
            fuenteAmbito.setBold(true);
            estiloAmbito.setFont(fuenteAmbito);

            // Blanco y negro (v3.10): la cabecera se distingue por la negrita y
            // por su linea inferior, no por un relleno de color. Un reporte que
            // se imprime en una impresora comun no puede depender del color, y
            // un relleno oscuro se lleva la tinta sin aportar nada.
            CellStyle estiloCabecera = libro.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteCabecera = libro.createFont();
            fuenteCabecera.setBold(true);
            estiloCabecera.setFont(fuenteCabecera);
            estiloCabecera.setAlignment(HorizontalAlignment.CENTER);
            estiloCabecera.setBorderBottom(BorderStyle.MEDIUM);
            estiloCabecera.setBorderTop(BorderStyle.THIN);
            estiloCabecera.setBorderLeft(BorderStyle.THIN);
            estiloCabecera.setBorderRight(BorderStyle.THIN);

            // RF-52b: el reporte dice de quien es antes de decir que contiene.
            Row filaSede = hoja.createRow(0);
            Cell celdaSede = filaSede.createCell(0);
            celdaSede.setCellValue(cabecera.sede());
            celdaSede.setCellStyle(estiloTitulo);

            Row filaAmbito = hoja.createRow(1);
            Cell celdaAmbito = filaAmbito.createCell(0);
            celdaAmbito.setCellValue(cabecera.ambito());
            celdaAmbito.setCellStyle(estiloAmbito);

            hoja.createRow(2).createCell(0).setCellValue("Reporte de inventario de bienes");

            hoja.createRow(3).createCell(0).setCellValue("Generado el "
                    + LocalDateTime.now().format(FECHA) + " | Registros: " + bienes.size());

            Row filaCabecera = hoja.createRow(5);
            for (int i = 0; i < CABECERAS.length; i++) {
                Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(CABECERAS[i]);
                celda.setCellStyle(estiloCabecera);
            }

            int numeroFila = 6;
            int correlativo = 1;
            for (EquipoDto bien : bienes) {
                Row fila = hoja.createRow(numeroFila++);
                String[] valores = valores(bien, correlativo++);
                for (int i = 0; i < valores.length; i++) {
                    fila.createCell(i).setCellValue(valores[i]);
                }
            }

            for (int i = 0; i < CABECERAS.length; i++) {
                hoja.autoSizeColumn(i);
            }
            hoja.createFreezePane(0, 6);

            libro.write(salida);
            return salida.toByteArray();

        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel.", ex);
        }
    }

    // ------------------------------------------------------------------
    // CSV
    // ------------------------------------------------------------------

    @Override
    public byte[] aCsv(List<EquipoDto> bienes, Cabecera cabecera) {
        StringBuilder sb = new StringBuilder();
        // BOM (U+FEFF) para que Excel reconozca UTF-8 al abrir el archivo directamente.
        sb.append('﻿');

        // RF-52b: las mismas cuatro lineas de encabezado que el Excel y el PDF,
        // cada una en su propia fila y sin separadores, para que al abrirlo en
        // una hoja de calculo caigan en la primera columna y no se confundan
        // con los datos.
        sb.append(escaparCsv(cabecera.sede())).append('\n');
        sb.append(escaparCsv(cabecera.ambito())).append('\n');
        sb.append(escaparCsv("Reporte de inventario de bienes")).append('\n');
        sb.append(escaparCsv("Generado el " + LocalDateTime.now().format(FECHA)
                + " | Registros: " + bienes.size())).append('\n');
        sb.append('\n');

        sb.append(String.join(";", CABECERAS)).append('\n');

        int correlativo = 1;
        for (EquipoDto bien : bienes) {
            String[] valores = valores(bien, correlativo++);
            sb.append(String.join(";", Arrays.stream(valores).map(this::escaparCsv).toList()));
            sb.append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ");
        return (limpio.contains(";") || limpio.contains("\"")) ? "\"" + limpio + "\"" : limpio;
    }

    // ------------------------------------------------------------------
    // PDF
    // ------------------------------------------------------------------

    @Override
    public byte[] aPdf(List<EquipoDto> bienes, Cabecera cabecera) {
        // Se omiten las columnas mas extensas para que el reporte quepa en A4 horizontal.
        String[] cabecerasPdf = {"N°", "Laboratorio", "Nombre", "Marca", "Modelo", "N° Serie",
                "Cod. Inventario", "Cod. Patrimonial", "Categoria", "Condicion", "Costo (S/)"};

        // Indices dentro de valores(): mantener alineados con CABECERAS.
        int[] columnasPdf = {0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12};

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4.rotate(), 28, 28, 32, 28);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            // Blanco y negro (v3.10): el reporte se imprime y se archiva, y en
            // una impresora comun el color solo resta legibilidad. La jerarquia
            // la marcan el tamano, la negrita y las lineas de la tabla.
            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fuenteAmbito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font fuenteCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            // RF-52b: el reporte dice de quien es antes de decir que contiene.
            Paragraph titulo = new Paragraph(cabecera.sede(), fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph ambito = new Paragraph(cabecera.ambito(), fuenteAmbito);
            ambito.setAlignment(Element.ALIGN_CENTER);
            documento.add(ambito);

            Paragraph queEs = new Paragraph("Reporte de inventario de bienes", fuenteSubtitulo);
            queEs.setAlignment(Element.ALIGN_CENTER);
            documento.add(queEs);

            Paragraph subtitulo = new Paragraph(
                    "Generado el " + LocalDateTime.now().format(FECHA) + "  |  Registros: " + bienes.size(),
                    fuenteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(12f);
            documento.add(subtitulo);

            PdfPTable tabla = new PdfPTable(cabecerasPdf.length);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4f, 12f, 17f, 9f, 10f, 11f, 11f, 11f, 12f, 10f, 8f});
            tabla.setHeaderRows(1);

            for (String rotulo : cabecerasPdf) {
                PdfPCell celda = new PdfPCell(new Phrase(rotulo, fuenteCabecera));
                // Sin relleno de color: la cabecera se distingue por la negrita
                // y por su linea inferior gruesa.
                celda.setBorderColor(Color.BLACK);
                celda.setBorderWidthBottom(1.2f);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setPadding(5f);
                tabla.addCell(celda);
            }

            int correlativo = 1;
            for (EquipoDto bien : bienes) {
                String[] valores = valores(bien, correlativo++);
                for (int indice : columnasPdf) {
                    String valor = valores[indice];
                    PdfPCell celda = new PdfPCell(new Phrase(valor == null ? "" : valor, fuenteCelda));
                    // Las filas se separan con sus propias lineas y no con un
                    // fondo alterno, que en blanco y negro seria un gris que
                    // ensucia la lectura y gasta toner.
                    celda.setBorderColor(Color.BLACK);
                    celda.setBorderWidth(0.4f);
                    celda.setPadding(4f);
                    tabla.addCell(celda);
                }
            }

            documento.add(tabla);
            documento.close();
            return salida.toByteArray();

        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el archivo PDF: " + ex.getMessage(), ex);
        }
    }

    // ------------------------------------------------------------------

    private String[] valores(EquipoDto e, int correlativo) {
        return new String[]{
                String.valueOf(correlativo),
                texto(e.coordinacion()),
                texto(e.laboratorio()),
                texto(e.nombre()),
                texto(e.marca()),
                texto(e.modelo()),
                texto(e.numeroSerie()),
                texto(e.codigoInventario()),
                texto(e.codigoPatrimonial()),
                texto(e.categoria()),
                e.condicion().getEtiqueta(),
                e.fechaAdquisicion() == null ? "" : e.fechaAdquisicion().format(SOLO_FECHA),
                e.costo() == null ? "" : e.costo().toPlainString(),
                texto(e.responsableEquipo()),
                e.fechaRegistro() != null ? e.fechaRegistro().format(FECHA) : "",
                texto(e.registradoPor()),
                texto(e.motivoBaja())
        };
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }
}
