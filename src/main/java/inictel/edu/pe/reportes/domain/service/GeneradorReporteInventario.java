package inictel.edu.pe.reportes.domain.service;

import inictel.edu.pe.inventario.application.dto.EquipoDto;

import java.util.List;

/**
 * Puerto de generacion de los archivos de exportacion del inventario (RF-23).
 *
 * <p>El contexto de reportes decide que se exporta, con que filtros y a nombre
 * de que dependencia; como se construye cada formato —Apache POI para Excel,
 * OpenPDF para PDF— es un detalle de infraestructura.</p>
 */
public interface GeneradorReporteInventario {

    /**
     * Encabezado institucional del reporte (RF-52b).
     *
     * <p>Un listado de bienes sin decir de quien son no sirve para presentarlo
     * a nadie: en papel, el reporte tiene que nombrarse a si mismo. Los tres
     * datos son los de la jerarquia del sistema —institucion, Direccion y
     * Coordinacion (RN-01, RF-10, RF-11)— y los dos ultimos faltan cuando el
     * Administrador exporta la institucion entera, que es el unico caso en que
     * el reporte no pertenece a una sola dependencia.</p>
     *
     * @param sede         "Sede Central INICTEL-UNI", constante del sistema
     * @param direccion    nombre completo de la Direccion, o {@code null}
     * @param coordinacion nombre de la Coordinacion, o {@code null}
     */
    record Cabecera(String sede, String direccion, String coordinacion) {

        /** La linea que identifica el ambito, ya redactada. */
        public String ambito() {
            if (direccion == null && coordinacion == null) {
                return "Todas las direcciones y coordinaciones";
            }
            if (coordinacion == null) {
                return direccion;
            }
            return direccion == null ? coordinacion : direccion + "  |  " + coordinacion;
        }
    }

    byte[] aExcel(List<EquipoDto> bienes, Cabecera cabecera);

    byte[] aCsv(List<EquipoDto> bienes, Cabecera cabecera);

    byte[] aPdf(List<EquipoDto> bienes, Cabecera cabecera);
}
