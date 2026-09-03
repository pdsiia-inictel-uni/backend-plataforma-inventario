package inictel.edu.pe.inventario.application.dto;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.model.MovimientoEquipo;
import inictel.edu.pe.inventario.domain.model.TipoMovimiento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * La autoria de cada hecho del historial del bien (RF-56).
 *
 * <p>Desde la v3.12 la ficha del bien ya no dice quien lo registro: el dato se
 * consulta en el historial, y por eso el alta lo lleva rotulado.</p>
 */
class MovimientoDtoTest {

    @Test
    @DisplayName("RF-56: el alta dice 'Registrado por:' delante del nombre")
    void elAltaLlevaRotulo() {
        MovimientoDto dto = MovimientoDto.de(movimiento(TipoMovimiento.ALTA, "Ana Diaz", Rol.OPERADOR));

        assertEquals("Registrado por: Ana Diaz (Operador)", dto.autoria());
    }

    @Test
    @DisplayName("RF-56: los demas hechos ya llevan su verbo en el titulo y no lo repiten")
    void losDemasNoLoLlevan() {
        MovimientoDto dto = MovimientoDto.de(
                movimiento(TipoMovimiento.BAJA, "Ana Diaz", Rol.RESPONSABLE));

        assertEquals("Ana Diaz (Responsable)", dto.autoria());
        assertEquals("Dado de baja", dto.tipoEtiqueta());
    }

    @Test
    @DisplayName("RNF-47: sin rol registrado, la autoria queda con el nombre a secas")
    void sinRolSoloElNombre() {
        MovimientoDto dto = MovimientoDto.de(movimiento(TipoMovimiento.ALTA, "Ana Diaz", null));

        assertEquals("Registrado por: Ana Diaz", dto.autoria());
    }

    @Test
    @DisplayName("RNF-47: el historial nombra tambien al autor que no consta")
    void sinAutorLoDice() {
        MovimientoDto dto = MovimientoDto.de(movimiento(TipoMovimiento.ALTA, null, null));

        assertEquals("Registrado por: Usuario no disponible", dto.autoria());
        assertEquals("Usuario no disponible", dto.usuario());
    }

    private static MovimientoEquipo movimiento(TipoMovimiento tipo, String autor, Rol rol) {
        return MovimientoEquipo.reconstituir(1L, 7L, tipo, null, CondicionEquipo.OPERATIVO,
                null, autor == null ? null : 3L, autor, rol, LocalDateTime.now());
    }
}
