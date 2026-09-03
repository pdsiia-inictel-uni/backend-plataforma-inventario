package inictel.edu.pe.iam.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import inictel.edu.pe.compartido.domain.seguridad.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas del agregado Usuario que no dependen de la persistencia.
 */
class UsuarioTest {

    private Usuario personaRegistrada() {
        return Usuario.registrar(
                new NombreUsuario("jperez"),
                new NombrePersona("Juan", "Perez", "Lopez"),
                new Dni("12345678"),
                "Investigador",
                new CorreoInstitucional("jperez@inictel-uni.edu.pe"),
                "$2a$10$hashficticio");
    }

    private Usuario operadorDe(long coordinacion) {
        Usuario usuario = personaRegistrada();
        usuario.asignarA(coordinacion, Rol.OPERADOR);
        return usuario;
    }

    @Test
    @DisplayName("RF-24: el DNI debe tener exactamente 8 digitos")
    void dniInvalidoEsRechazado() {
        assertThrows(DatosInvalidosException.class, () -> new Dni("1234"));
        assertThrows(DatosInvalidosException.class, () -> new Dni("1234567A"));
        assertEquals("12345678", new Dni(" 12345678 ").valor());
    }

    @Test
    @DisplayName("RF-16: los dos apellidos son obligatorios en toda alta")
    void losDosApellidosSonObligatorios() {
        assertThrows(DatosInvalidosException.class,
                () -> new NombrePersona("Juan", "Perez", null));
        assertThrows(DatosInvalidosException.class,
                () -> new NombrePersona("Juan", "Perez", "   "));

        assertEquals("Juan Perez Lopez",
                new NombrePersona(" Juan ", " Perez ", " Lopez ").completo());
    }

    @Test
    @DisplayName("RF-06: un usuario nuevo nace obligado a cambiar su contrasena")
    void usuarioNuevoDebeCambiarPassword() {
        assertTrue(personaRegistrada().isDebeCambiarPassword());
    }

    @Test
    @DisplayName("RNF-05: la cuenta se bloquea al alcanzar el maximo de intentos fallidos")
    void bloqueoTrasIntentosFallidos() {
        Usuario usuario = personaRegistrada();

        assertFalse(usuario.registrarIntentoFallido(3, 15));
        assertFalse(usuario.registrarIntentoFallido(3, 15));
        assertTrue(usuario.registrarIntentoFallido(3, 15), "el tercer intento debe bloquear la cuenta");

        assertTrue(usuario.estaBloqueado());
        assertEquals(0, usuario.getIntentosFallidos(), "el contador se reinicia al bloquear");
    }

    @Test
    @DisplayName("Un acceso correcto libera el contador y el bloqueo")
    void accesoExitosoLiberaBloqueo() {
        Usuario usuario = personaRegistrada();
        usuario.registrarIntentoFallido(3, 15);
        usuario.registrarAccesoExitoso();

        assertFalse(usuario.estaBloqueado());
        assertEquals(0, usuario.getIntentosFallidos());
    }

    @Test
    @DisplayName("RF-06: cambiar la contrasena cancela la exigencia de cambio")
    void cambioDePasswordCancelaExigencia() {
        Usuario usuario = personaRegistrada();
        usuario.cambiarPassword("$2a$10$otrohash");

        assertFalse(usuario.isDebeCambiarPassword());
        assertTrue(usuario.tieneMismoHash("$2a$10$otrohash"));
    }

    // ------------------------------------------------------------------
    // Registro previo y asignacion de puestos (ERS v3.3)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RF-16b: el alta deja a la persona registrada, sin rol y sin coordinacion")
    void elAltaNoDaPuesto() {
        Usuario usuario = personaRegistrada();

        assertTrue(usuario.estaSinAsignar());
        assertNull(usuario.getRol());
        assertEquals(Set.of(), usuario.getCoordinaciones());
        // Existe y esta activa: lo que le falta es un puesto, no la cuenta.
        assertTrue(usuario.estaActiva());
    }

    @Test
    @DisplayName("RF-28d: la asignacion le da rol y coordinacion")
    void laAsignacionDaElPuesto() {
        Usuario usuario = operadorDe(7L);

        assertFalse(usuario.estaSinAsignar());
        assertTrue(usuario.esOperador());
        assertTrue(usuario.perteneceA(7L));
        assertFalse(usuario.perteneceA(8L));
    }

    @Test
    @DisplayName("RN-05: un responsable pertenece a una sola coordinacion")
    void elResponsablePerteneceAUnaSola() {
        Usuario usuario = personaRegistrada();
        usuario.asignarA(7L, Rol.RESPONSABLE);

        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(8L, Rol.RESPONSABLE));

        assertTrue(usuario.esResponsable());
        assertEquals(Set.of(7L), usuario.getCoordinaciones());
        assertEquals(7L, usuario.getCoordinacion());
    }

    @Test
    @DisplayName("RN-05: un operador pertenece a una sola coordinacion")
    void elOperadorPerteneceAUnaSola() {
        Usuario usuario = operadorDe(7L);

        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(8L, Rol.OPERADOR));
        assertEquals(Set.of(7L), usuario.getCoordinaciones());
    }

    @Test
    @DisplayName("RN-05: mover a otra coordinacion exige retirar antes la que se tiene")
    void moverDeCoordinacionExigeRetirarPrimero() {
        Usuario usuario = operadorDe(7L);

        usuario.retirarDe(7L);
        usuario.asignarA(8L, Rol.OPERADOR);

        assertEquals(Set.of(8L), usuario.getCoordinaciones());
        assertEquals(8L, usuario.getCoordinacion());
    }

    @Test
    @DisplayName("RF-28d: cambiar de rol exige retirar antes las asignaciones vivas")
    void noSeMezclanDosRoles() {
        Usuario usuario = operadorDe(7L);

        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(8L, Rol.RESPONSABLE));
    }

    @Test
    @DisplayName("RN-05: el administrador no pertenece a ninguna coordinacion")
    void elAdminNoSeAdscribe() {
        Usuario usuario = personaRegistrada();
        usuario.asignarComoAdministrador();

        assertTrue(usuario.esAdmin());
        assertEquals(Set.of(), usuario.getCoordinaciones());
        assertFalse(usuario.perteneceA(7L));
    }

    @Test
    @DisplayName("RN-05: quien tiene coordinaciones no puede ser nombrado administrador sin dejarlas")
    void elAdminNoArrastraCoordinaciones() {
        Usuario usuario = operadorDe(7L);

        assertThrows(ReglaNegocioException.class, usuario::asignarComoAdministrador);
    }

    // ------------------------------------------------------------------
    // RN-33: el puesto se da a quien no tiene ninguno
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RN-33: quien ya es operador no recibe otro puesto sin darle de baja antes")
    void elOperadorNoRecibeOtroPuesto() {
        Usuario usuario = operadorDe(7L);

        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(7L, Rol.RESPONSABLE));
        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(7L, Rol.OPERADOR));
        assertThrows(ReglaNegocioException.class, usuario::asignarComoAdministrador);

        assertTrue(usuario.esOperador(), "el puesto que tenia sigue intacto");
        assertEquals(Set.of(7L), usuario.getCoordinaciones());
    }

    @Test
    @DisplayName("RN-33: quien ya es administrador no pasa a responsable ni a operador")
    void elAdminNoCambiaDePuesto() {
        Usuario usuario = personaRegistrada();
        usuario.asignarComoAdministrador();

        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(7L, Rol.RESPONSABLE));
        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(7L, Rol.OPERADOR));
        assertThrows(ReglaNegocioException.class, usuario::asignarComoAdministrador);

        assertTrue(usuario.esAdmin());
        assertEquals(Set.of(), usuario.getCoordinaciones());
    }

    @Test
    @DisplayName("RN-33, RN-08: dada la baja, la misma persona vuelve a poder recibir un puesto")
    void trasLaBajaVuelveAEstarLibre() {
        Usuario usuario = personaRegistrada();
        usuario.asignarA(7L, Rol.RESPONSABLE);

        usuario.retirarDe(7L);
        usuario.asignarComoAdministrador();

        assertTrue(usuario.esAdmin());
    }

    @Test
    @DisplayName("RF-28d: una cuenta que no puede entrar no recibe puesto")
    void inactivoNoRecibePuesto() {
        Usuario usuario = personaRegistrada();
        usuario.suspender();

        assertThrows(ReglaNegocioException.class, () -> usuario.asignarA(9L, Rol.RESPONSABLE));
    }

    @Test
    @DisplayName("RF-28d: retirar la ultima asignacion devuelve a la persona al registro previo")
    void retirarLaUltimaDejaSinPuesto() {
        Usuario usuario = operadorDe(7L);

        usuario.retirarDe(7L);

        // Dejar un puesto no es dejar la institucion (RN-09).
        assertTrue(usuario.estaSinAsignar());
        assertTrue(usuario.estaActiva());
        assertEquals(Set.of(), usuario.getCoordinaciones());
    }

    @Test
    @DisplayName("RF-26: dar de baja al responsable lo deja libre y con su cuenta activa")
    void laBajaDelResponsableNoDesactivaLaCuenta() {
        Usuario usuario = personaRegistrada();
        usuario.asignarA(7L, Rol.RESPONSABLE);

        usuario.retirarDe(7L);

        assertTrue(usuario.estaSinAsignar(), "vuelve al estado en que quedo tras el alta");
        assertTrue(usuario.estaActiva());
        assertNull(usuario.getCoordinacion());
    }

    @Test
    @DisplayName("RF-28d: no se retira a alguien de una coordinacion en la que no esta")
    void noSeRetiraDeDondeNoEsta() {
        Usuario usuario = operadorDe(7L);

        assertThrows(ReglaNegocioException.class, () -> usuario.retirarDe(8L));
    }

    @Test
    @DisplayName("RF-22b: la suspension es temporal y conserva rol y coordinacion")
    void desactivarYReactivarConservaTodo() {
        Usuario usuario = operadorDe(7L);
        usuario.suspender();
        assertFalse(usuario.estaActiva());
        assertEquals(Set.of(7L), usuario.getCoordinaciones(), "de vacaciones no se pierde el puesto");

        usuario.reactivar();
        assertTrue(usuario.estaActiva());
        assertTrue(usuario.esOperador());
        assertEquals(Set.of(7L), usuario.getCoordinaciones());
    }

    // ------------------------------------------------------------------
    // RF-22b, RN-34: la baja es dejar la institucion; la suspension, no
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RN-34: la baja libera el puesto; irse de la institucion no es irse de vacaciones")
    void laBajaLiberaElPuesto() {
        Usuario usuario = operadorDe(7L);

        usuario.darDeBaja();

        assertTrue(usuario.estaDeBaja());
        assertFalse(usuario.estaActiva());
        assertTrue(usuario.estaSinAsignar(), "quien se fue no responde por un inventario");
        assertEquals(Set.of(), usuario.getCoordinaciones());
    }

    @Test
    @DisplayName("RF-22b: la reincorporacion devuelve la cuenta, no el puesto")
    void laReincorporacionNoDevuelveElPuesto() {
        Usuario usuario = operadorDe(7L);
        usuario.darDeBaja();

        usuario.reincorporar();

        assertTrue(usuario.estaActiva());
        assertTrue(usuario.estaSinAsignar(), "el puesto se cubrio cuando se fue");
    }

    @Test
    @DisplayName("RF-22b: suspender y dar de baja no se pisan")
    void lasTransicionesImposiblesSeRechazan() {
        Usuario usuario = personaRegistrada();
        assertThrows(ReglaNegocioException.class, usuario::reincorporar);

        usuario.darDeBaja();
        assertThrows(ReglaNegocioException.class, usuario::suspender);
        assertThrows(ReglaNegocioException.class, usuario::reactivar);
        assertThrows(ReglaNegocioException.class, usuario::darDeBaja);
    }

    @Test
    @DisplayName("RF-28d: la cuenta suspendida o de baja no recibe puesto")
    void sinCuentaActivaNoHayPuesto() {
        Usuario suspendida = personaRegistrada();
        suspendida.suspender();
        assertThrows(ReglaNegocioException.class, () -> suspendida.asignarA(7L, Rol.OPERADOR));

        Usuario deBaja = personaRegistrada();
        deBaja.darDeBaja();
        assertThrows(ReglaNegocioException.class, () -> deBaja.asumirResponsabilidadDe(7L));
    }

    // ------------------------------------------------------------------
    // RF-26b, RN-35: el relevo del Responsable
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RN-35: el operador de la coordinacion asciende y deja su plaza de operador")
    void elOperadorAsciendeEnSuCoordinacion() {
        Usuario usuario = operadorDe(7L);

        usuario.asumirResponsabilidadDe(7L);

        assertTrue(usuario.esResponsable());
        assertEquals(Set.of(7L), usuario.getCoordinaciones(), "sigue en una sola coordinacion");
    }

    @Test
    @DisplayName("RN-35: quien no tiene puesto y el administrador tambien pueden tomarlo")
    void elLibreYElAdminPuedenTomarElPuesto() {
        Usuario libre = personaRegistrada();
        libre.asumirResponsabilidadDe(7L);
        assertTrue(libre.esResponsable());

        Usuario admin = personaRegistrada();
        admin.asignarComoAdministrador();
        admin.asumirResponsabilidadDe(8L);
        assertTrue(admin.esResponsable(), "deja de ser administrador al bajar al terreno");
        assertEquals(Set.of(8L), admin.getCoordinaciones());
    }

    @Test
    @DisplayName("RN-35: nadie responde por dos inventarios a la vez")
    void elResponsableNoTomaOtraCoordinacion() {
        Usuario usuario = personaRegistrada();
        usuario.asumirResponsabilidadDe(7L);

        assertThrows(ReglaNegocioException.class, () -> usuario.asumirResponsabilidadDe(8L));
        assertEquals(Set.of(7L), usuario.getCoordinaciones());
    }

    @Test
    @DisplayName("RF-26: el responsable saliente queda libre, con su cuenta intacta")
    void elSalienteQuedaLibre() {
        Usuario saliente = personaRegistrada();
        saliente.asumirResponsabilidadDe(7L);

        saliente.liberarPuesto();

        assertTrue(saliente.estaSinAsignar());
        assertTrue(saliente.estaActiva(), "dejar el cargo no es dejar la institucion");
        assertEquals(Set.of(), saliente.getCoordinaciones());
    }
}
