package inictel.edu.pe.organizacion.domain.service;

/**
 * Puerto hacia el inventario y los prestamos, necesario para decidir si una
 * Coordinacion o un Laboratorio pueden desactivarse (RF-13, RF-14).
 *
 * <p>Es una capa anticorrupcion: {@code organizacion} solo pregunta cuantos
 * bienes hay, nunca conoce el agregado Equipo ni sus tablas (RNF-39).</p>
 */
public interface CensoDeBienes {

    /** Bienes no dados de baja que pertenecen a la Coordinacion. */
    long bienesActivosEn(Long coordinacionId);

    /** Bienes ubicados en el Laboratorio, sin importar su condicion. */
    long bienesUbicadosEn(Long laboratorioId);

    /** Prestamos sin devolver de la Coordinacion. */
    long prestamosVigentesEn(Long coordinacionId);

    /** Conteo de bienes por condicion, para el resumen de la estructura (RF-15). */
    ResumenBienes resumenDe(Long coordinacionId);

    record ResumenBienes(long operativos, long prestados, long enMantenimiento, long dadosDeBaja) {

        public static ResumenBienes vacio() {
            return new ResumenBienes(0, 0, 0, 0);
        }

        public long total() {
            return operativos + prestados + enMantenimiento + dadosDeBaja;
        }
    }
}
