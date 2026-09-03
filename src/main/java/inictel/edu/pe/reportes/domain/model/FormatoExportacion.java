package inictel.edu.pe.reportes.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

import java.util.Locale;

/**
 * Formatos admitidos para la exportacion del inventario (RF-23).
 */
public enum FormatoExportacion {

    EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    CSV("csv", "text/csv"),
    PDF("pdf", "application/pdf");

    private final String extension;
    private final String tipoContenido;

    FormatoExportacion(String extension, String tipoContenido) {
        this.extension = extension;
        this.tipoContenido = tipoContenido;
    }

    public String getExtension() {
        return extension;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    /** Acepta las variantes que usa el frontend: xlsx, excel, csv y pdf. */
    public static FormatoExportacion desde(String valor) {
        String limpio = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        return switch (limpio) {
            case "xlsx", "excel" -> EXCEL;
            case "csv" -> CSV;
            case "pdf" -> PDF;
            default -> throw new DatosInvalidosException("formato",
                    "Formato de exportacion no admitido. Use xlsx, csv o pdf.");
        };
    }
}
