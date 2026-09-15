package inictel.edu.pe.organizacion.domain.service;

/**
 * Puerto hacia el inventario, necesario para describir las coordinaciones y
 * sus laboratorios (RF-12, RF-15).
 *
 * <p>Es una capa anticorrupcion: {@code organizacion} solo pregunta cuantos
 * bienes hay, nunca conoce el agregado Equipo ni sus tablas (RNF-39).</p>
 */
public interface CensoDeBienes {

    /** Conteo de bienes por condicion, para el resumen de la estructura (RF-15). */
    ResumenBienes resumenDe(Long coordinacionId);

    /**
     * RF-12: bienes ubicados en el Laboratorio, por condicion.
     */
    ResumenBienes resumenDeLaboratorio(Long laboratorioId);

    record ResumenBienes(long operativos, long prestados, long enMantenimiento, long dadosDeBaja) {

        public static ResumenBienes vacio() {
            return new ResumenBienes(0, 0, 0, 0);
        }

        public long total() {
            return operativos + prestados + enMantenimiento + dadosDeBaja;
        }

        /** Bienes que siguen en servicio: todos salvo los dados de baja. */
        public long vigentes() {
            return operativos + prestados + enMantenimiento;
        }
    }
}
