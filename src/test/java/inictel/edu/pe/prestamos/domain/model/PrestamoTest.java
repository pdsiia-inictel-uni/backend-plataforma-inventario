package inictel.edu.pe.prestamos.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas del agregado Prestamo.
 */
class PrestamoTest {

    private static final BienPrestado BIEN = new BienPrestado(1L, 7L, "Dron", "INV-0007", "DJI-99");
    private static final OperadorPrestamo OPERADOR = new OperadorPrestamo(2L, "Ana Torres");

    private Prestamo nuevoPrestamo(LocalDate estimada) {
        return Prestamo.registrar(
                BIEN,
                new PersonaResponsable("Carlos Ramirez", "45678912"),
                "Laboratorio de campo",
                estimada,
                "Sale con maleta y dos baterias",
                OPERADOR);
    }

    @Test
    @DisplayName("RF-59: el DNI de la persona debe tener 8 digitos")
    void dniPersonaValidado() {
        assertThrows(DatosInvalidosException.class,
                () -> new PersonaResponsable("Carlos Ramirez", "123"));
    }

    @Test
    @DisplayName("RF-59: la fecha estimada de devolucion no puede ser pasada")
    void fechaEstimadaNoPuedeSerPasada() {
        assertThrows(DatosInvalidosException.class, () -> nuevoPrestamo(LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("Las fechas del prestamo las genera el servidor")
    void fechaPrestamoLaGeneraElServidor() {
        Prestamo prestamo = nuevoPrestamo(null);
        assertNotNull(prestamo.getFechaPrestamo());
        assertTrue(prestamo.estaActivo());
    }

    @Test
    @DisplayName("RF-65: la devolucion no conforme exige detallar el estado del bien")
    void devolucionNoConformeExigeObservaciones() {
        Prestamo prestamo = nuevoPrestamo(null);

        assertThrows(DatosInvalidosException.class,
                () -> prestamo.registrarDevolucion(false, false, "   ", OPERADOR));
    }

    @Test
    @DisplayName("RF-64: la devolucion no conforme marca el bien para revision")
    void devolucionNoConformeMarcaRevision() {
        Prestamo prestamo = nuevoPrestamo(null);
        prestamo.registrarDevolucion(false, false, "Llega con la carcasa rota", OPERADOR);

        assertTrue(prestamo.isReportaDano());
        assertEquals(EstadoPrestamo.DEVUELTO, prestamo.getEstado());
        assertNotNull(prestamo.getFechaDevolucion());
    }

    @Test
    @DisplayName("Un prestamo devuelto no puede devolverse otra vez")
    void noSeDevuelveDosVeces() {
        Prestamo prestamo = nuevoPrestamo(null);
        prestamo.registrarDevolucion(true, false, null, OPERADOR);

        assertThrows(ReglaNegocioException.class,
                () -> prestamo.registrarDevolucion(true, false, null, OPERADOR));
    }

    @Test
    @DisplayName("RF-67: se considera vencido el activo que supero la fecha estimada")
    void vencimientoYDiasDeAtraso() {
        Prestamo prestamo = Prestamo.reconstituir(1L, BIEN,
                new PersonaResponsable("Carlos Ramirez", "45678912"), null,
                java.time.LocalDateTime.now().minusDays(10), LocalDate.now().minusDays(3),
                null, null, null, false, null, OPERADOR, null, EstadoPrestamo.ACTIVO);

        assertTrue(prestamo.estaVencido());
        assertEquals(3, prestamo.diasAtraso());

        prestamo.registrarDevolucion(true, false, null, OPERADOR);
        assertFalse(prestamo.estaVencido());
        assertEquals(0, prestamo.diasAtraso());
    }
}
