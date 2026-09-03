package inictel.edu.pe.compartido.domain.seguridad;

import inictel.edu.pe.compartido.domain.excepcion.AccesoDenegadoException;

/**
 * Instantanea del usuario que ejecuta la operacion en curso.
 *
 * <p>Ademas de quien actua, transporta las dos coordenadas de autorizacion del
 * sistema: su rol y la Coordinacion en la que tiene su puesto. Con ellas
 * cualquier contexto puede aplicar el aislamiento exigido por RN-23 sin
 * consultar a {@code iam}.</p>
 *
 * <p>La Coordinacion es una sola (RN-05): tanto el RESPONSABLE como el
 * OPERADOR pertenecen a exactamente una, de modo que el ambito de una peticion
 * no es algo que el cliente elija. El servidor lo sabe, y lo sabe entero
 * (RNF-10).</p>
 *
 * @param coordinacion Coordinacion del puesto; {@code null} para el ADMIN, que
 *                     no pertenece a ninguna, y para quien esta registrado sin
 *                     puesto (RF-16b)
 */
public record UsuarioAutenticado(
        Long id,
        String username,
        String nombreCompleto,
        Rol rol,
        Long coordinacion) {

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean esResponsable() {
        return rol == Rol.RESPONSABLE;
    }

    public boolean esOperador() {
        return rol == Rol.OPERADOR;
    }

    /**
     * Coordinacion en la que se ejecuta la operacion, exigida.
     *
     * <p>Se usa en las operaciones que solo tienen sentido dentro de una
     * Coordinacion. Un ADMIN que llegue aqui es un error de autorizacion: sus
     * endpoints nunca deben invocar estos casos de uso (RN-22).</p>
     */
    public Long coordinacionRequerida() {
        if (coordinacion == null) {
            throw new AccesoDenegadoException(
                    "Esta operacion pertenece a una coordinacion y su cuenta no tiene ninguna asignada.");
        }
        return coordinacion;
    }

    /**
     * Verifica que el usuario puede operar sobre la Coordinacion indicada.
     *
     * <p>El Administrador queda excluido a proposito: no escribe en ninguna
     * Coordinacion. Para sus consultas de solo lectura existe
     * {@link #puedeLeerCoordinacion(Long)}.</p>
     */
    public void exigirAccesoA(Long objetivo) {
        if (objetivo == null || !objetivo.equals(coordinacion)) {
            throw new AccesoDenegadoException(
                    "No tiene acceso a los datos de otra coordinacion.");
        }
    }

    /** El ADMIN lee toda la institucion; los demas, solo lo suyo. */
    public boolean puedeLeerCoordinacion(Long objetivo) {
        return esAdmin() || (objetivo != null && objetivo.equals(coordinacion));
    }

    /**
     * Coordinacion sobre la que se resuelve una consulta de listado.
     *
     * <p>RNF-10: el cliente nunca decide a que Coordinacion tiene acceso, eso
     * lo sabe el servidor. Si pide una que no le corresponde, la peticion se
     * rechaza en lugar de reinterpretarse: devolver los datos propios ante una
     * pregunta por datos ajenos oculta el error al frontend y contradice el
     * criterio de aceptacion, que exige 403.</p>
     *
     * @param solicitada la que pidio el cliente, o {@code null} si no pidio ninguna
     * @return para el ADMIN, la solicitada ({@code null} = toda la institucion);
     *         para el resto, siempre la suya
     */
    public Long ambitoDeConsulta(Long solicitada) {
        if (esAdmin()) {
            return solicitada;
        }
        if (solicitada != null) {
            exigirAccesoA(solicitada);
            return solicitada;
        }
        return coordinacionRequerida();
    }
}
