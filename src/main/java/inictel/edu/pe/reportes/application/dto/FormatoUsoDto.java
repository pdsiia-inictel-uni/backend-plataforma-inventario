package inictel.edu.pe.reportes.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * El formato de registro de uso, listo para imprimirse (RF-78).
 *
 * <p>Reune lo que el sistema sabe del bien —punto 2— con lo que quedo
 * guardado en el registro de uso externo: su apertura (puntos 1, 3, 4 y 5) y,
 * si ya se cerro, su cierre (puntos 6, 7, 8 y 10). El punto 9, las firmas,
 * sale en blanco para firmarse a mano. El generador no consulta nada mas.</p>
 */
public record FormatoUsoDto(

        // Cabecera
        Long numeroRegistro,
        String sede,
        String direccion,
        String coordinacion,
        String laboratorio,

        // 1. Responsable del equipamiento (Coordinador) e investigador encargado
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

        // 5. Registro de uso: fin real si ya se cerro, previsto si no
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

        // 10. Observaciones generales
        String observaciones,

        /** El registro ya tiene su parte final; si no, el documento lo advierte. */
        boolean cerrado) {
}
