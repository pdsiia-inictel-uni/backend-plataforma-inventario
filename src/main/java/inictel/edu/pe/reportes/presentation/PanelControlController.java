package inictel.edu.pe.reportes.presentation;

import inictel.edu.pe.reportes.application.dto.PanelControlDto;
import inictel.edu.pe.reportes.application.service.PanelControlServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Panel de control (RF-75 .. RF-77).
 *
 * <p>Accesible a los tres roles; el servicio compone el panel que corresponde
 * a cada uno a partir de su rol y su coordinacion.</p>
 */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes - Panel", description = "Indicadores del inventario y de los prestamos")
public class PanelControlController {

    private final PanelControlServicio panel;

    public PanelControlController(PanelControlServicio panel) {
        this.panel = panel;
    }

    @GetMapping("/panel")
    @Operation(summary = "Indicadores del panel segun el rol del usuario (RF-75 .. RF-77)")
    public PanelControlDto resumen() {
        return panel.resumen();
    }
}
