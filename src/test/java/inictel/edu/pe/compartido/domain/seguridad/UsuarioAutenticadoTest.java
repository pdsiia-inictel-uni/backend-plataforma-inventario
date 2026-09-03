package inictel.edu.pe.compartido.domain.seguridad;

import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Aislamiento por coordinacion (RN-23, RNF-10) resuelto en el dominio, sin
 * Spring ni base de datos.
 */
class UsuarioAutenticadoTest {

    private static final Long PROPIA = 5L;
    private static final Long AJENA = 9L;

    private UsuarioAutenticado admin() {
        return new UsuarioAutenticado(1L, "admin", "Administrador del Sistema", Rol.ADMIN, null);
    }

    private UsuarioAutenticado responsable() {
        return new UsuarioAutenticado(2L, "anadiaz", "Ana Diaz", Rol.RESPONSABLE, PROPIA);
    }

    private UsuarioAutenticado operador() {
        return new UsuarioAutenticado(3L, "lrojas", "Luis Rojas", Rol.OPERADOR, PROPIA);
    }

    @Test
    @DisplayName("RF-49: el Administrador consulta la coordinacion que elija")
    void elAdministradorEligeLaCoordinacion() {
        assertEquals(AJENA, admin().ambitoDeConsulta(AJENA));
    }

    @Test
    @DisplayName("RF-46: sin coordinacion elegida, el Administrador consulta toda la institucion")
    void elAdministradorSinEleccionConsultaTodo() {
        assertNull(admin().ambitoDeConsulta(null));
    }

    @Test
    @DisplayName("RNF-10: el servidor deduce del token la coordinacion del usuario operativo")
    void alUsuarioOperativoSeLeImponeLaSuya() {
        assertEquals(PROPIA, responsable().ambitoDeConsulta(null));
        assertEquals(PROPIA, operador().ambitoDeConsulta(null));
        assertEquals(PROPIA, responsable().ambitoDeConsulta(PROPIA));
    }

    @Test
    @DisplayName("RNF-10: pedir otra coordinacion se rechaza, no se reinterpreta")
    void pedirOtraCoordinacionSeRechaza() {
        assertThrows(AccesoDenegadoException.class, () -> responsable().ambitoDeConsulta(AJENA));
        assertThrows(AccesoDenegadoException.class, () -> operador().ambitoDeConsulta(AJENA));
    }

    @Test
    @DisplayName("RN-05: una cuenta operativa sin puesto no puede consultar nada")
    void cuentaOperativaSinPuesto() {
        UsuarioAutenticado huerfano = new UsuarioAutenticado(4L, "x", "X", Rol.OPERADOR, null);
        assertThrows(AccesoDenegadoException.class, () -> huerfano.ambitoDeConsulta(null));
    }

    @Test
    @DisplayName("RN-05: el responsable trabaja en la unica coordinacion que tiene")
    void elResponsableTrabajaEnLaSuya() {
        assertEquals(PROPIA, responsable().coordinacionRequerida());
        assertThrows(AccesoDenegadoException.class, () -> responsable().exigirAccesoA(AJENA));
    }

    @Test
    @DisplayName("RN-23: solo el ADMIN lee coordinaciones distintas a la suya")
    void lecturaDeOtraCoordinacion() {
        assertEquals(true, admin().puedeLeerCoordinacion(AJENA));
        assertEquals(false, responsable().puedeLeerCoordinacion(AJENA));
        assertEquals(true, responsable().puedeLeerCoordinacion(PROPIA));
    }
}
