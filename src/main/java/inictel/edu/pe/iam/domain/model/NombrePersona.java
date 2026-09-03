package inictel.edu.pe.iam.domain.model;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;

/**
 * Nombre completo de la persona segun el registro institucional (RF-16):
 * nombres, primer apellido y segundo apellido, los tres obligatorios.
 *
 * <p>Hasta la v3.4 el segundo apellido era opcional en el alta que hace el
 * Administrador, con el argumento de que quien registra a un tercero puede no
 * conocerlo. En la practica se omitia por prisa y quedaba una persona a medio
 * identificar en un sistema donde la identidad es exactamente lo que permite
 * no duplicar personal ni confundir a dos homonimos. Quien da de alta a
 * alguien tiene su DNI delante; sus dos apellidos, tambien.</p>
 */
public record NombrePersona(String nombres, String primerApellido, String segundoApellido) {

    private static final int LONGITUD_MAXIMA = 100;

    public NombrePersona {
        nombres = exigir(nombres, "nombres", "Ingrese los nombres.");
        primerApellido = exigir(primerApellido, "primerApellido", "Ingrese el primer apellido.");
        segundoApellido = exigir(segundoApellido, "segundoApellido", "Ingrese el segundo apellido.");
    }

    /** Forma usada en listados y reportes. */
    public String completo() {
        return nombres + ' ' + primerApellido + ' ' + segundoApellido;
    }

    private static String exigir(String valor, String campo, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new DatosInvalidosException(campo, mensaje);
        }
        String limpio = valor.trim();
        if (limpio.length() > LONGITUD_MAXIMA) {
            throw new DatosInvalidosException(campo, "El texto no puede superar los 100 caracteres.");
        }
        return limpio;
    }

}
