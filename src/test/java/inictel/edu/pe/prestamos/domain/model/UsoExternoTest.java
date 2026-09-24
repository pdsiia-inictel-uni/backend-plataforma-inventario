package inictel.edu.pe.prestamos.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Registro de uso externo en dos partes (RF-78). */
class UsoExternoTest {

    private static final BienPrestado BIEN = new BienPrestado(1L, 10L, "Espectrógrafo", "INV-1", "SN-1");
    private static final OperadorPrestamo RESPONSABLE = new OperadorPrestamo(5L, "Ana Díaz");
    private static final LocalDate HOY = LocalDate.of(2026, 9, 23);
    private static final LocalTime NUEVE = LocalTime.of(9, 0);

    private UsoExterno abierto() {
        return UsoExterno.abrir(BIEN, "Operativo", "Luis Rojas", null, null,
                "Carla Pérez", null, null, "Proyecto PAO", HOY, NUEVE, null, null, "Mediciones", RESPONSABLE);
    }

    @Test
    @DisplayName("La apertura exige investigador encargado y usuario")
    void apertura() {
        UsoExterno uso = abierto();
        assertThat(uso.estaAbierto()).isTrue();

        assertThatThrownBy(() -> UsoExterno.abrir(BIEN, "Operativo", " ", null, null,
                "Carla", null, null, null, HOY, NUEVE, null, null, null, RESPONSABLE))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    @DisplayName("La fecha y la hora de inicio se escriben a mano y son obligatorias")
    void inicioManual() {
        UsoExterno uso = abierto();
        assertThat(uso.getFechaInicio()).isEqualTo(HOY.atTime(NUEVE));

        assertThatThrownBy(() -> UsoExterno.abrir(BIEN, "Operativo", "Luis", null, null,
                "Carla", null, null, null, null, NUEVE, null, null, null, RESPONSABLE))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> UsoExterno.abrir(BIEN, "Operativo", "Luis", null, null,
                "Carla", null, null, null, HOY, null, null, null, null, RESPONSABLE))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    @DisplayName("El fin previsto no puede ser anterior al inicio")
    void finPrevistoNoAnteriorAlInicio() {
        assertThatThrownBy(() -> UsoExterno.abrir(BIEN, "Operativo", "Luis", null, null,
                "Carla", null, null, null, HOY, NUEVE, HOY.minusDays(1), null, null, RESPONSABLE))
                .isInstanceOf(DatosInvalidosException.class);
        assertThatThrownBy(() -> UsoExterno.abrir(BIEN, "Operativo", "Luis", null, null,
                "Carla", null, null, null, HOY, NUEVE, HOY, LocalTime.of(8, 0), null, RESPONSABLE))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    @DisplayName("El cierre exige las dos conformidades")
    void cierreExigeConformidad() {
        UsoExterno uso = abierto();
        assertThatThrownBy(() -> uso.cerrar(null, true, null, null, null, RESPONSABLE))
                .isInstanceOf(DatosInvalidosException.class);
        assertThat(uso.estaAbierto()).isTrue();
    }

    @Test
    @DisplayName("Devuelto en malas condiciones exige describir el incidente y reporta daño")
    void devueltoMal() {
        UsoExterno uso = abierto();
        assertThatThrownBy(() -> uso.cerrar(true, false, " ", null, null, RESPONSABLE))
                .isInstanceOf(DatosInvalidosException.class);

        uso.cerrar(true, false, "Lente rayado", "Se envió a revisión", null, RESPONSABLE);
        assertThat(uso.estaAbierto()).isFalse();
        assertThat(uso.reportaDano()).isTrue();
        assertThat(uso.getFechaCierre()).isNotNull();
    }

    @Test
    @DisplayName("Solo se anula un uso abierto")
    void anulacion() {
        UsoExterno uso = abierto();
        uso.exigirAnulable();
        uso.cerrar(true, true, null, null, null, RESPONSABLE);
        assertThatThrownBy(uso::exigirAnulable).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("Un uso cerrado no se vuelve a cerrar")
    void noSeCierraDosVeces() {
        UsoExterno uso = abierto();
        uso.cerrar(true, true, null, null, null, RESPONSABLE);
        assertThat(uso.reportaDano()).isFalse();
        assertThatThrownBy(() -> uso.cerrar(true, true, null, null, null, RESPONSABLE))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
