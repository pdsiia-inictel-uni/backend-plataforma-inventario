package inictel.edu.pe.iam.application.dto;

import inictel.edu.pe.compartido.domain.seguridad.Rol;
import inictel.edu.pe.iam.domain.model.EstadoCuenta;
import inictel.edu.pe.iam.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Vista de lectura de un usuario. Nunca incluye el hash de la contrasena
 * (RF-05, RNF-46).
 *
 * <p>Desde la v3.3 el rol puede venir vacio —la persona esta registrada pero
 * aun sin puesto (RF-16b)— y las coordinaciones son una lista, porque un
 * Responsable puede estar a cargo de varias (RN-05).</p>
 */
public record UsuarioDto(
        Long id,
        String username,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String nombreCompleto,
        String dni,
        String cargo,
        String correo,
        Rol rol,
        String rolEtiqueta,
        boolean sinAsignar,
        List<CoordinacionAsignada> coordinaciones,
        EstadoCuenta estado,
        String estadoEtiqueta,
        /** RN-34: puesto que tenia al darse de baja; null si la cuenta esta activa. */
        Rol ultimoRol,
        CoordinacionAsignada ultimaCoordinacion,
        boolean debeCambiarPassword,
        /** RF-06b: solo la cuenta administradora inicial declara su identidad al estrenarse. */
        boolean debeCompletarIdentidad,
        boolean bloqueado,
        LocalDateTime ultimoAcceso,
        LocalDateTime fechaCreacion) {

    /**
     * Coordinacion en la que la persona tiene puesto, con su nombre y el de su
     * Direccion ya resueltos.
     *
     * <p>La Direccion viaja con ella porque el sistema nombra siempre el ambito
     * entero —"Coordinacion de Redes, Direccion de Investigacion y Desarrollo
     * Tecnologico"—: la coordinacion sola no dice de que parte de la casa se
     * trata (RF-01b, ERS 8.1).</p>
     */
    public record CoordinacionAsignada(Long id, String nombre, String direccionNombre) {
    }

    /** Texto del rol para la interfaz; RNF-29: vocabulario del dominio. */
    public static final String SIN_ASIGNAR = "Sin asignar";

    public static UsuarioDto de(Usuario u, List<CoordinacionAsignada> coordinaciones) {
        return de(u, coordinaciones, null);
    }

    public static UsuarioDto de(Usuario u,
                                List<CoordinacionAsignada> coordinaciones,
                                CoordinacionAsignada ultimaCoordinacion) {
        return new UsuarioDto(
                u.getId(),
                u.getUsername().valor(),
                u.getNombre().nombres(),
                u.getNombre().primerApellido(),
                u.getNombre().segundoApellido(),
                u.nombreCompleto(),
                u.getDni().valor(),
                u.getCargo(),
                u.getCorreo().valor(),
                u.getRol(),
                etiquetaDeRol(u),
                // Quien esta de baja no es trabajo pendiente de asignar: primero
                // habria que reincorporarlo (RN-34).
                u.estaSinAsignar() && u.estaActiva(),
                coordinaciones == null ? List.of() : List.copyOf(coordinaciones),
                u.getEstado(),
                u.getEstado().getEtiqueta(),
                u.getUltimoRol(),
                ultimaCoordinacion,
                u.isDebeCambiarPassword(),
                u.debeCompletarIdentidad(),
                u.estaBloqueado(),
                u.getUltimoAcceso(),
                u.getFechaCreacion());
    }

    /**
     * El rol que se lee en la lista. Quien esta de baja ya no tiene ninguno,
     * pero se le sigue nombrando por el que tenia: "Operador" y la insignia
     * "De baja" dicen juntos lo que paso.
     */
    private static String etiquetaDeRol(Usuario u) {
        if (u.getRol() != null) {
            return u.getRol().getEtiqueta();
        }
        if (u.getUltimoRol() != null) {
            return u.getUltimoRol().getEtiqueta();
        }
        return SIN_ASIGNAR;
    }
}
