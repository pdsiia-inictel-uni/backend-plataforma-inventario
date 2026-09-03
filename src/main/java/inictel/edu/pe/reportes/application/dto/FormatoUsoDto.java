package inictel.edu.pe.reportes.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * El formato de registro de uso, listo para imprimirse (RF-78).
 *
 * <p>Reune las dos mitades del documento: la que el sistema conoce del bien
 * —puntos 1 y 2, y la condicion en que esta ahora mismo— y la que escribio
 * quien lo genera. El generador no consulta nada mas: recibe el documento
 * entero y solo decide como se dibuja.</p>
 *
 * <p>Este objeto no se persiste en ninguna tabla. Vive lo que dura la
 * peticion (RN-36).</p>
 */
public record FormatoUsoDto(

        // Cabecera
        String sede,
        String direccion,
        String coordinacion,
        String laboratorio,

        // 1. Responsable del equipamiento
        String responsableEquipamiento,
        String investigadorEncargado,
        String correoEncargado,
        String celularEncargado,

        // 2. Identificacion del equipo (del sistema)
        String descripcion,
        String codigoPatrimonial,
        String codigoInventario,
        String marca,
        String modelo,
        String numeroSerie,
        BigDecimal valorCompra,
        LocalDate fechaAdquisicion,
        String areaInvestigacion,
        String estadoEquipo,

        // 3. Datos del usuario
        String nombreInvestigador,
        String correoInvestigador,
        String telefonoInvestigador,

        // 4. Proyecto asociado y actividad
        String proyecto,

        // 5. Registro de uso
        LocalDateTime inicioUso,
        LocalDate fechaFinUso,
        LocalTime horaFinUso,
        String actividadRealizada,

        // 6 y 7. Conformidad de entrega y de devolucion
        Boolean entregadoOperativo,
        Boolean devueltoOperativo,

        // 8. Incidentes
        String incidente,
        String accionCorrectiva,

        // 9. Firmas
        String coordinadorQueFirma,

        // 10. Observaciones generales
        String observaciones) {
}
