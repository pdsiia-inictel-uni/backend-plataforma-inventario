package inictel.edu.pe.inventario.presentation;

import inictel.edu.pe.compartido.domain.CriterioPagina;
import inictel.edu.pe.compartido.presentation.PaginaResponse;
import inictel.edu.pe.inventario.application.dto.EquipoDto;
import inictel.edu.pe.inventario.application.dto.EquipoResumenDto;
import inictel.edu.pe.inventario.application.dto.MovimientoDto;
import inictel.edu.pe.inventario.application.dto.ResponsableEquipoDto;
import inictel.edu.pe.inventario.application.service.GestionInventarioServicio;
import inictel.edu.pe.inventario.domain.model.CondicionEquipo;
import inictel.edu.pe.inventario.domain.repository.FiltroEquipos;
import inictel.edu.pe.inventario.presentation.dto.EquipoRequest;
import inictel.edu.pe.inventario.presentation.dto.MotivoRequest;
import inictel.edu.pe.inventario.presentation.dto.ResponsableEquipoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Inventario de bienes (RF-34 .. RF-57).
 *
 * <p>Las anotaciones de rol son la primera barrera; el servicio vuelve a
 * comprobar rol y Coordinacion en cada operacion, porque solo el conoce a que
 * Coordinacion pertenece el bien (RN-23).</p>
 *
 * <p>Ninguna operacion acepta al ADMIN en escritura: su rol es estructural, no
 * operativo (RN-22).</p>
 */
@RestController
@RequestMapping("/api/equipos")
@Tag(name = "Inventario", description = "Bienes de cada coordinacion, su condicion y su historial")
public class EquipoController {

    private final GestionInventarioServicio inventario;

    public EquipoController(GestionInventarioServicio inventario) {
        this.inventario = inventario;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /**
     * RF-47: por defecto solo los bienes operativos. {@code todas=true} o una
     * {@code condicion} explicita cambian la vista.
     *
     * @param responsableEquipoId RF-84: deja solo los bienes que esa persona
     *                            tiene a su nombre. Es el listado por
     *                            responsable de equipo: el Operador consulta
     *                            los suyos enviando su propio identificador y
     *                            el Responsable revisa los de cada uno de los
     *                            suyos
     * @param sinResponsable      RF-84: deja solo los que no estan asignados a
     *                            ningun Operador, que son los que lleva el
     *                            propio Responsable de la Coordinacion (RN-37)
     */
    @GetMapping
    @Operation(summary = "Lista los bienes; por defecto solo los operativos (RF-47, RF-84)")
    public PaginaResponse<EquipoResumenDto> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long coordinacionId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long laboratorioId,
            @RequestParam(required = false) CondicionEquipo condicion,
            @RequestParam(defaultValue = "false") boolean todas,
            @RequestParam(required = false) Long responsableEquipoId,
            @RequestParam(defaultValue = "false") boolean sinResponsable,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "false") boolean descendente) {

        FiltroEquipos filtro = new FiltroEquipos(
                coordinacionId, q, categoriaId, laboratorioId, condicion, todas,
                responsableEquipoId, sinResponsable);

        return PaginaResponse.de(inventario.buscar(
                filtro, CriterioPagina.de(pagina, tamano, ordenarPor, descendente)));
    }

    /**
     * RF-84: quien lleva equipos en la coordinacion y cuantos lleva cada uno.
     *
     * <p>Es el indice del listado por responsable de equipo: da las opciones
     * del desplegable con su recuento, de modo que quien lo abre ve el reparto
     * antes de elegir y no acaba en una lista vacia (RNF-23). La primera fila
     * es la del Responsable de la coordinacion, cuyos bienes son los que no
     * tienen asignacion (RN-37).</p>
     *
     * <p>Lo consultan los tres roles: el Operador para encontrarse a si mismo,
     * el Responsable para repartir y el Administrador para revisar una
     * coordinacion, que debe indicar (RF-49).</p>
     */
    @GetMapping("/responsables")
    @Operation(summary = "Reparto del inventario: quien tiene equipos a su cargo y cuantos (RF-84)")
    public List<ResponsableEquipoDto> responsablesDeEquipo(
            @RequestParam(required = false) Long coordinacionId) {
        return inventario.responsablesDeEquipo(coordinacionId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ficha completa de un bien")
    public EquipoDto obtener(@PathVariable Long id) {
        return inventario.obtener(id);
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Linea de tiempo completa del bien (RF-56)")
    public List<MovimientoDto> historial(@PathVariable Long id) {
        return inventario.historial(id);
    }

    // ------------------------------------------------------------------
    // Escritura
    // ------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','OPERADOR')")
    @Operation(summary = "Registra un bien en la coordinacion del usuario (RF-34)")
    public ResponseEntity<EquipoDto> crear(@Valid @RequestBody EquipoRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventario.crear(peticion.aComando(null)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Edita un bien. Solo el responsable de su coordinacion (RF-40)")
    public EquipoDto editar(@PathVariable Long id, @Valid @RequestBody EquipoRequest peticion) {
        return inventario.editar(peticion.aComando(id));
    }

    /**
     * RF-83: pone el bien a cargo de un Operador de la Coordinacion.
     *
     * <p>Con {@code responsableEquipoId} nulo el bien vuelve a cargo del
     * Responsable, que es como nace y donde queda cuando se le retira a un
     * Operador (RN-37). Exclusivo del Responsable: es quien reparte el trabajo
     * dentro de su Coordinacion y quien responde por el inventario.</p>
     */
    @PutMapping("/{id}/responsable-equipo")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Asigna o retira el operador que tiene el bien a su cargo (RF-83)")
    public EquipoDto asignarResponsableDeEquipo(@PathVariable Long id,
                                                @RequestBody(required = false)
                                                ResponsableEquipoRequest peticion) {
        return inventario.asignarResponsableDeEquipo(id,
                peticion == null ? null : peticion.responsableEquipoId());
    }

    @PostMapping("/{id}/mantenimiento")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Envia el bien a mantenimiento (RF-41)")
    public EquipoDto enviarAMantenimiento(@PathVariable Long id, @Valid @RequestBody MotivoRequest peticion) {
        return inventario.enviarAMantenimiento(id, peticion.motivo());
    }

    @PostMapping("/{id}/operativo")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Devuelve el bien a condicion operativa o cierra su revision (RF-41)")
    public EquipoDto devolverAOperativo(@PathVariable Long id,
                                        @RequestBody(required = false) MotivoRequest peticion) {
        return inventario.devolverAOperativo(id, peticion == null ? null : peticion.motivo());
    }

    @PostMapping("/{id}/baja")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Da de baja un bien. Baja logica con motivo (RF-42)")
    public EquipoDto darDeBaja(@PathVariable Long id, @Valid @RequestBody MotivoRequest peticion) {
        return inventario.darDeBaja(id, peticion.motivo());
    }

    @PostMapping("/{id}/reincorporar")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Reincorpora al inventario un bien dado de baja (RF-43)")
    public EquipoDto reincorporar(@PathVariable Long id, @Valid @RequestBody MotivoRequest peticion) {
        return inventario.reincorporar(id, peticion.motivo());
    }

    /**
     * RF-51, RF-51c: adjunta o sustituye la fotografia del bien.
     *
     * <p>Exclusivo del Responsable desde la v3.10: sobre un bien ya registrado
     * solo escribe el, y la fotografia es un dato del bien como cualquier otro
     * (RF-45). El Operador la aporta al registrar el equipo, dentro del alta,
     * que es cuando el bien todavia es suyo (RF-51b).</p>
     */
    @PostMapping(value = "/{id}/foto", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('RESPONSABLE')")
    @Operation(summary = "Adjunta la fotografia del bien. Solo el responsable de su coordinacion (RF-51)")
    public EquipoDto subirFoto(@PathVariable Long id, @RequestPart("archivo") MultipartFile archivo) {
        try {
            return inventario.asignarFoto(id, archivo.getOriginalFilename(),
                    archivo.getContentType(), archivo.getInputStream());
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer el archivo recibido.", ex);
        }
    }
}
