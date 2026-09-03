package inictel.edu.pe.prestamos.domain.service;

import inictel.edu.pe.prestamos.domain.model.BienPrestado;

import java.util.Optional;

/**
 * Capa anticorrupcion hacia el contexto {@code inventario} (RNF-39).
 *
 * <p>Prestamos no conoce el agregado Bien: solo necesita saber si un bien puede
 * salir, obtener sus datos identificatorios y comunicar los cambios de
 * disponibilidad. La implementacion traduce estas intenciones a la API que
 * publica inventario.</p>
 */
public interface CatalogoBienes {

    /** Datos identificatorios del bien, si existe. */
    Optional<BienPrestado> buscar(Long equipoId);

    /** RN-15: true si el bien esta activo y en condicion operativa. */
    boolean estaDisponible(Long equipoId);

    /** Descripcion de la condicion actual, para explicar por que no puede prestarse. */
    String condicionActual(Long equipoId);

    /** true si el bien fue dado de baja. */
    boolean estaDeBaja(Long equipoId);

    /** RF-60: marca el bien como Prestado y lo anota en su historial. */
    void marcarPrestado(Long equipoId, String detalle);

    /**
     * RF-63, RF-64: devuelve el bien a Operativo. Si se reporto dano, queda
     * marcado para revision del Responsable (RN-19).
     */
    void registrarRetorno(Long equipoId, boolean reportaDano, String detalle);
}
