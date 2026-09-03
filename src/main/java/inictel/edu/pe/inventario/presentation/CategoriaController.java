package inictel.edu.pe.inventario.presentation;

import inictel.edu.pe.inventario.application.dto.CategoriaDto;
import inictel.edu.pe.inventario.application.service.GestionCategoriasServicio;
import inictel.edu.pe.inventario.presentation.dto.CategoriaRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Categorias de bienes y plantillas de atributos (RF-12, RF-13).
 * La consulta esta abierta a ambos roles; la escritura, solo al Administrador.
 */
@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Inventario - Categorias", description = "Modulos de bienes y sus atributos sugeridos")
public class CategoriaController {

    private final GestionCategoriasServicio categorias;

    public CategoriaController(GestionCategoriasServicio categorias) {
        this.categorias = categorias;
    }

    @GetMapping
    @Operation(summary = "Lista las categorias, opcionalmente solo las activas")
    public List<CategoriaDto> listar(@RequestParam(defaultValue = "true") boolean soloActivas) {
        return categorias.listar(soloActivas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una categoria con su plantilla de atributos")
    public CategoriaDto obtener(@PathVariable Long id) {
        return categorias.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crea una categoria. Solo Administrador (RF-12)")
    public ResponseEntity<CategoriaDto> crear(@Valid @RequestBody CategoriaRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorias.crear(peticion.aComando(null)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Edita una categoria y su plantilla. Solo Administrador (RF-13)")
    public CategoriaDto editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest peticion) {
        return categorias.editar(peticion.aComando(id));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activa o desactiva una categoria. Solo Administrador")
    public CategoriaDto cambiarEstado(@PathVariable Long id, @RequestParam boolean activa) {
        return categorias.cambiarEstado(id, activa);
    }
}
