package inictel.edu.pe.organizacion.domain.model;

/**
 * La institucion es una constante del sistema (RN-01).
 *
 * <p>No se persiste ni se administra: no existe alta, edicion ni baja de
 * instituciones. Se modela como constante para que el nombre tenga un unico
 * origen de verdad y la jerarquia quede completa desde el dominio.</p>
 */
public final class Institucion {

    public static final String NOMBRE = "INICTEL-UNI";

    /**
     * Como se nombra la institucion en un documento (RF-52b).
     *
     * <p>Los reportes que se imprimen y se archivan llevan la sede, no solo las
     * siglas: un listado de bienes sin decir de quien son no se le puede
     * presentar a nadie. Se declara aqui, junto al nombre, para que los dos
     * tengan un unico origen de verdad.</p>
     */
    public static final String SEDE = "Sede Central " + NOMBRE;

    private Institucion() {
    }
}
