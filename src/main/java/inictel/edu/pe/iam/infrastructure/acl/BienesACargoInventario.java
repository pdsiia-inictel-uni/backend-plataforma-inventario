package inictel.edu.pe.iam.infrastructure.acl;

import inictel.edu.pe.iam.domain.service.BienesACargo;
import inictel.edu.pe.inventario.application.service.ServicioPublicoInventario;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador anticorrupcion de {@code iam} hacia {@code inventario} (RNF-39).
 *
 * <p>Solo cruza por aqui lo que exige RN-38, y siempre a traves del servicio
 * de host abierto del otro contexto: {@code iam} no conoce el agregado Bien ni
 * su repositorio.</p>
 */
@Component
public class BienesACargoInventario implements BienesACargo {

    private final ServicioPublicoInventario inventario;

    public BienesACargoInventario(ServicioPublicoInventario inventario) {
        this.inventario = inventario;
    }

    @Override
    public long contarDe(Long usuarioId) {
        return inventario.contarBienesACargoDe(usuarioId);
    }

    @Override
    public List<BienACargo> listarDe(Long usuarioId) {
        return inventario.listarBienesACargoDe(usuarioId).stream()
                .map(bien -> new BienACargo(
                        bien.id(), bien.nombre(), bien.codigoInventario(), bien.laboratorio()))
                .toList();
    }

    @Override
    public void devolverAlResponsableLosDe(Long usuarioId) {
        inventario.devolverAlResponsableLosBienesDe(usuarioId);
    }
}
