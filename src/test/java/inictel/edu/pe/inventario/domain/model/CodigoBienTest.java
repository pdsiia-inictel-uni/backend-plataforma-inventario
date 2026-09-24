package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Codigo patrimonial: exactamente 12 caracteres, letras y/o numeros. */
class CodigoBienTest {

    @ParameterizedTest
    @ValueSource(strings = {"123456789012", "ABCDEFGHIJKL", "74AB12CD9901"})
    @DisplayName("Admite 12 caracteres de letras, numeros o ambos")
    void admiteAlfanumericos(String valor) {
        assertThat(CodigoBien.patrimonial(valor).valor()).isEqualTo(valor);
    }

    @Test
    @DisplayName("Se guarda en mayusculas y sin espacios alrededor")
    void normalizaAMayusculas() {
        assertThat(CodigoBien.patrimonial("  74ab12cd9901 ").valor()).isEqualTo("74AB12CD9901");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901", "1234567890123", "74AB-12CD-99", "74AB 12CD990", "ÑBCDEFGHIJKL"})
    @DisplayName("Rechaza otra longitud o caracteres que no sean letras y numeros")
    void rechazaInvalidos(String valor) {
        assertThatThrownBy(() -> CodigoBien.patrimonial(valor))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    @DisplayName("El codigo de inventario no tiene esa restriccion")
    void inventarioLibre() {
        assertThat(CodigoBien.inventario("INV-24-0031").valor()).isEqualTo("INV-24-0031");
    }
}
