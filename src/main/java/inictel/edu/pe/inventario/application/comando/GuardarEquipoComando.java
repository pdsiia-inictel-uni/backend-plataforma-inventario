package inictel.edu.pe.inventario.application.comando;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Alta o edicion de un bien (RF-34, RF-40).
 *
 * <p>No lleva condicion, coordinacion, responsable ni fechas: todos esos los
 * decide el servidor (RF-35 .. RF-37, RN-21). Lo que el cliente no puede
 * enviar, no puede falsear.</p>
 *
 * @param id nulo cuando se trata de un alta
 */
public record GuardarEquipoComando(
        Long id,
        String nombre,
        String marca,
        String modelo,
        String numeroSerie,
        String codigoInventario,
        String codigoPatrimonial,
        Long categoriaId,
        Long laboratorioId,
        LocalDate fechaAdquisicion,
        BigDecimal costo,
        String observaciones) {
}
