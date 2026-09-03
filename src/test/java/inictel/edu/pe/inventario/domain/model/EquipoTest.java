package inictel.edu.pe.inventario.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reglas patrimoniales del agregado Bien (RN-11 .. RN-19, RN-25).
 *
 * <p>El dominio es Java puro: estas pruebas no levantan Spring ni base de
 * datos (RNF-38).</p>
 */
class EquipoTest {

    private static final Long COORDINACION = 7L;
    private static final Long OTRA_COORDINACION = 9L;
    private static final Long LABORATORIO = 3L;
    private static final Long RESPONSABLE = 11L;
    private static final Long OPERADOR = 12L;
    private static final Long OTRO_OPERADOR = 13L;
    private static final LocalDate ADQUIRIDO = LocalDate.of(2024, 11, 18);

    private static Equipo unEquipo() {
        return Equipo.registrar(
                COORDINACION, LABORATORIO, "Laptop", "Dell", "E5470",
                new NumeroSerie("SN-001"),
                CodigoBien.inventario("INV-001"),
                CodigoBien.patrimonial("PAT-001"),
                new ReferenciaCategoria(1L, "Equipos de computo"),
                ADQUIRIDO, new BigDecimal("3500.00"), "Sin novedad",
                RESPONSABLE, OPERADOR);
    }

    private static Equipo unEquipoAdquiridoEl(LocalDate fecha) {
        return Equipo.registrar(
                COORDINACION, LABORATORIO, "Laptop", "Dell", "E5470",
                new NumeroSerie("SN-009"),
                CodigoBien.inventario("INV-009"),
                CodigoBien.patrimonial("PAT-009"),
                new ReferenciaCategoria(1L, "Equipos de computo"),
                fecha, new BigDecimal("3500.00"), null,
                RESPONSABLE, OPERADOR);
    }

    @Nested
    @DisplayName("Registro (RF-34 .. RF-37)")
    class Registro {

        @Test
        @DisplayName("RN-13: todo bien nace en condicion OPERATIVO")
        void naceOperativo() {
            assertThat(unEquipo().getCondicion()).isEqualTo(CondicionEquipo.OPERATIVO);
            assertThat(unEquipo().estaDisponible()).isTrue();
            assertThat(unEquipo().isRevisionPendiente()).isFalse();
        }

        @Test
        @DisplayName("RF-36: queda a cargo del responsable, no de quien lo registra")
        void seSellaConElResponsableVigente() {
            Equipo equipo = unEquipo();
            assertThat(equipo.getResponsableId()).isEqualTo(RESPONSABLE);
            assertThat(equipo.getUsuarioRegistroId()).isEqualTo(OPERADOR);
        }

        @Test
        @DisplayName("RF-34: la fecha de alta la genera el servidor")
        void fechaGeneradaPorElServidor() {
            assertThat(unEquipo().getFechaRegistro()).isNotNull();
        }

        @Test
        @DisplayName("RN-11: el bien debe pertenecer a una coordinacion")
        void exigeCoordinacion() {
            assertThatThrownBy(() -> Equipo.registrar(
                    null, null, "Laptop", "Dell", "E5470",
                    new NumeroSerie("SN-002"),
                    CodigoBien.inventario("INV-002"),
                    CodigoBien.patrimonial("PAT-002"),
                    new ReferenciaCategoria(1L, "Equipos de computo"),
                    ADQUIRIDO, BigDecimal.TEN, null, RESPONSABLE, OPERADOR))
                    .isInstanceOf(DatosInvalidosException.class);
        }

        @Test
        @DisplayName("RN-11: perteneceA distingue la coordinacion propia de la ajena")
        void reconoceSuCoordinacion() {
            Equipo equipo = unEquipo();
            assertThat(equipo.perteneceA(COORDINACION)).isTrue();
            assertThat(equipo.perteneceA(OTRA_COORDINACION)).isFalse();
        }

        @Test
        @DisplayName("RN-25: la fecha de adquisicion no puede ser futura")
        void rechazaFechaFutura() {
            assertThatThrownBy(() -> unEquipoAdquiridoEl(LocalDate.now().plusDays(1)))
                    .isInstanceOf(DatosInvalidosException.class)
                    .hasMessageContaining("no puede ser futura");
        }

        @Test
        @DisplayName("RN-25: la fecha de adquisicion no puede ser anterior a 1980")
        void rechazaFechaDemasiadoAntigua() {
            assertThatThrownBy(() -> unEquipoAdquiridoEl(LocalDate.of(1979, 12, 31)))
                    .isInstanceOf(DatosInvalidosException.class)
                    .hasMessageContaining("anterior");
        }

        @Test
        @DisplayName("RF-34: la fecha de adquisicion se guarda entera, no solo el anio")
        void conservaLaFechaCompleta() {
            assertThat(unEquipo().getFechaAdquisicion()).isEqualTo(LocalDate.of(2024, 11, 18));
        }

        @Test
        @DisplayName("Hoy mismo es una fecha de adquisicion valida")
        void admiteLaFechaDeHoy() {
            assertThatCode(() -> unEquipoAdquiridoEl(LocalDate.now())).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("RN-25: el costo no puede ser negativo")
        void rechazaCostoNegativo() {
            assertThatThrownBy(() -> Equipo.registrar(
                    COORDINACION, null, "Laptop", "Dell", "E5470",
                    new NumeroSerie("SN-004"),
                    CodigoBien.inventario("INV-004"),
                    CodigoBien.patrimonial("PAT-004"),
                    new ReferenciaCategoria(1L, "Equipos de computo"),
                    ADQUIRIDO, new BigDecimal("-1"), null, RESPONSABLE, OPERADOR))
                    .isInstanceOf(DatosInvalidosException.class)
                    .hasMessageContaining("negativo");
        }

        @Test
        @DisplayName("RN-14: el comodin S/N se reconoce como bien sin serie")
        void admiteBienesSinSerie() {
            assertThat(new NumeroSerie("s/n").esSinSerie()).isTrue();
        }

        @Test
        @DisplayName("Marca y modelo son obligatorios en el nuevo modelo")
        void exigeMarcaYModelo() {
            assertThatThrownBy(() -> Equipo.registrar(
                    COORDINACION, null, "Laptop", "  ", "E5470",
                    new NumeroSerie("SN-005"),
                    CodigoBien.inventario("INV-005"),
                    CodigoBien.patrimonial("PAT-005"),
                    new ReferenciaCategoria(1L, "Equipos de computo"),
                    ADQUIRIDO, BigDecimal.TEN, null, RESPONSABLE, OPERADOR))
                    .isInstanceOf(DatosInvalidosException.class);
        }
    }

    @Nested
    @DisplayName("Prestamo y devolucion (RN-15, RN-19)")
    class CicloDePrestamo {

        @Test
        @DisplayName("RN-15: solo se presta un bien operativo")
        void soloSePrestaLoOperativo() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.PRESTADO);
        }

        @Test
        @DisplayName("RN-16: un bien prestado no puede prestarse otra vez")
        void noSePrestaDosVeces() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            assertThatThrownBy(equipo::marcarComoPrestado)
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("no esta disponible");
        }

        @Test
        @DisplayName("RF-63: la devolucion conforme deja el bien operativo y sin alertas")
        void devolucionConforme() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            equipo.registrarRetorno(false);

            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.OPERATIVO);
            assertThat(equipo.isRevisionPendiente()).isFalse();
        }

        @Test
        @DisplayName("RN-19: la devolucion con dano NO envia a mantenimiento, solo marca revision")
        void devolucionConDanoMarcaRevision() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            equipo.registrarRetorno(true);

            // El operador no puede retirar un bien del servicio: decide el responsable.
            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.OPERATIVO);
            assertThat(equipo.isRevisionPendiente()).isTrue();
        }

        @Test
        @DisplayName("RN-19: el responsable cierra la revision devolviendo el bien a operativo")
        void elResponsableCierraLaRevision() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            equipo.registrarRetorno(true);

            equipo.devolverAOperativo();
            assertThat(equipo.isRevisionPendiente()).isFalse();
        }

        @Test
        @DisplayName("RN-19: o confirma el mantenimiento tras la revision")
        void elResponsableConfirmaMantenimiento() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            equipo.registrarRetorno(true);

            equipo.enviarAMantenimiento("Pantalla rota reportada en la devolucion");
            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.MANTENIMIENTO);
            assertThat(equipo.isRevisionPendiente()).isFalse();
        }
    }

    @Nested
    @DisplayName("Condicion, baja y reincorporacion (RN-17, RNF-47)")
    class CicloDeVida {

        @Test
        @DisplayName("RN-17: un bien prestado no puede darse de baja")
        void noSeDaDeBajaUnBienPrestado() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            assertThatThrownBy(() -> equipo.darDeBaja("Obsoleto"))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("devolucion");
        }

        @Test
        @DisplayName("RN-17: un bien prestado no puede pasar a mantenimiento")
        void noVaAMantenimientoUnBienPrestado() {
            Equipo equipo = unEquipo();
            equipo.marcarComoPrestado();
            assertThatThrownBy(() -> equipo.enviarAMantenimiento("Revision"))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("prestado");
        }

        @Test
        @DisplayName("RF-42: la baja exige motivo y es logica")
        void bajaExigeMotivo() {
            Equipo equipo = unEquipo();
            assertThatThrownBy(() -> equipo.darDeBaja("   "))
                    .isInstanceOf(DatosInvalidosException.class);

            equipo.darDeBaja("Equipo obsoleto");
            assertThat(equipo.isActivo()).isFalse();
            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.BAJA);
            assertThat(equipo.getMotivoBaja()).isEqualTo("Equipo obsoleto");
            assertThat(equipo.getFechaBaja()).isNotNull();
        }

        @Test
        @DisplayName("RF-40: un bien de baja no se edita hasta reincorporarlo")
        void bienDeBajaNoSeEdita() {
            Equipo equipo = unEquipo();
            equipo.darDeBaja("Obsoleto");

            assertThatThrownBy(() -> equipo.actualizarDatos(
                    null, "Otro", "HP", "X", new NumeroSerie("SN-9"),
                    CodigoBien.inventario("INV-9"), CodigoBien.patrimonial("PAT-9"),
                    new ReferenciaCategoria(1L, "Equipos de computo"),
                    ADQUIRIDO, BigDecimal.TEN, null))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("reincorporarlo");
        }

        @Test
        @DisplayName("RF-43: la reincorporacion devuelve el bien a operativo")
        void reincorporacion() {
            Equipo equipo = unEquipo();
            equipo.darDeBaja("Obsoleto");
            equipo.reincorporar();

            assertThat(equipo.isActivo()).isTrue();
            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.OPERATIVO);
            assertThat(equipo.getMotivoBaja()).isNull();
            assertThat(equipo.getFechaBaja()).isNull();
        }

        @Test
        @DisplayName("Un bien de baja no puede prestarse")
        void bienDeBajaNoSePresta() {
            Equipo equipo = unEquipo();
            equipo.darDeBaja("Obsoleto");
            assertThatThrownBy(equipo::marcarComoPrestado)
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("dado de baja");
        }

        @Test
        @DisplayName("RF-41: mantenimiento y retorno a operativo")
        void mantenimientoYRetorno() {
            Equipo equipo = unEquipo();
            equipo.enviarAMantenimiento("Cambio de bateria");
            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.MANTENIMIENTO);
            assertThat(equipo.estaDisponible()).isFalse();

            equipo.devolverAOperativo();
            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.OPERATIVO);
            assertThat(equipo.estaDisponible()).isTrue();
        }

        @Test
        @DisplayName("No se envia dos veces a mantenimiento el mismo bien")
        void mantenimientoNoEsIdempotente() {
            Equipo equipo = unEquipo();
            equipo.enviarAMantenimiento("Revision");
            assertThatThrownBy(() -> equipo.enviarAMantenimiento("Otra vez"))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("ya se encuentra en mantenimiento");
        }

        @Test
        @DisplayName("RF-40: la edicion no altera la condicion del bien")
        void laEdicionNoTocaLaCondicion() {
            Equipo equipo = unEquipo();
            equipo.enviarAMantenimiento("Revision");

            assertThatCode(() -> equipo.actualizarDatos(
                    null, "Laptop renombrada", "Dell", "E5470", new NumeroSerie("SN-001"),
                    CodigoBien.inventario("INV-001"), CodigoBien.patrimonial("PAT-001"),
                    new ReferenciaCategoria(1L, "Equipos de computo"),
                    ADQUIRIDO, new BigDecimal("3500.00"), "Actualizado"))
                    .doesNotThrowAnyException();

            assertThat(equipo.getCondicion()).isEqualTo(CondicionEquipo.MANTENIMIENTO);
        }
    }

    @Nested
    @DisplayName("Responsable del equipo (RF-83, RN-37)")
    class ResponsableDelEquipo {

        @Test
        @DisplayName("El bien nace sin operador a cargo: lo lleva el Responsable")
        void naceACargoDelResponsable() {
            Equipo equipo = unEquipo();
            assertThat(equipo.getResponsableEquipoId()).isNull();
            assertThat(equipo.estaACargoDeUnOperador()).isFalse();
        }

        @Test
        @DisplayName("Que lo registre un Operador no lo pone a su nombre")
        void registrarloNoEsTenerloACargo() {
            assertThat(unEquipo().getUsuarioRegistroId()).isEqualTo(OPERADOR);
            assertThat(unEquipo().getResponsableEquipoId()).isNull();
        }

        @Test
        @DisplayName("El Responsable lo pone a cargo de un Operador")
        void seEntregaAUnOperador() {
            Equipo equipo = unEquipo();
            equipo.ponerACargoDe(OPERADOR);

            assertThat(equipo.getResponsableEquipoId()).isEqualTo(OPERADOR);
            assertThat(equipo.estaACargoDeUnOperador()).isTrue();
        }

        @Test
        @DisplayName("Entregarselo a quien ya lo tiene se rechaza")
        void noSeEntregaDosVecesAlMismo() {
            Equipo equipo = unEquipo();
            equipo.ponerACargoDe(OPERADOR);

            assertThatThrownBy(() -> equipo.ponerACargoDe(OPERADOR))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("ya esta a cargo");
        }

        @Test
        @DisplayName("Pasa de un Operador a otro sin devolverlo antes")
        void cambiaDeOperador() {
            Equipo equipo = unEquipo();
            equipo.ponerACargoDe(OPERADOR);
            equipo.ponerACargoDe(OTRO_OPERADOR);

            assertThat(equipo.getResponsableEquipoId()).isEqualTo(OTRO_OPERADOR);
        }

        @Test
        @DisplayName("Devolverlo al Responsable lo deja sin operador a cargo")
        void vuelveAlResponsable() {
            Equipo equipo = unEquipo();
            equipo.ponerACargoDe(OPERADOR);
            equipo.devolverAlResponsableDeCoordinacion();

            assertThat(equipo.getResponsableEquipoId()).isNull();
            assertThat(equipo.estaACargoDeUnOperador()).isFalse();
        }

        @Test
        @DisplayName("RN-38: la baja del bien lo suelta, para no atar al operador a un equipo que ya no existe")
        void laBajaLoSuelta() {
            Equipo equipo = unEquipo();
            equipo.ponerACargoDe(OPERADOR);
            equipo.darDeBaja("Obsoleto");

            assertThat(equipo.getResponsableEquipoId()).isNull();
        }

        @Test
        @DisplayName("Un bien dado de baja no admite responsable")
        void elBienDeBajaNoSeEntrega() {
            Equipo equipo = unEquipo();
            equipo.darDeBaja("Obsoleto");

            assertThatThrownBy(() -> equipo.ponerACargoDe(OPERADOR))
                    .isInstanceOf(ReglaNegocioException.class);
        }
    }
}
