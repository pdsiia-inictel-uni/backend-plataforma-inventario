package inictel.edu.pe.inventario.application.service;

import inictel.edu.pe.compartido.domain.excepcion.DatosInvalidosException;
import inictel.edu.pe.compartido.domain.excepcion.RecursoNoEncontradoException;
import inictel.edu.pe.inventario.application.comando.GuardarCategoriaComando;
import inictel.edu.pe.inventario.application.dto.CategoriaDto;
import inictel.edu.pe.inventario.domain.model.Categoria;
import inictel.edu.pe.inventario.domain.repository.CategoriaRepositorio;
import inictel.edu.pe.inventario.domain.repository.EquipoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Casos de uso del catalogo institucional de categorias (RF-31 .. RF-33).
 *
 * <p>La escritura es exclusiva del Administrador; la lectura esta abierta
 * porque los formularios de inventario necesitan poblar su selector.</p>
 */
@Service
public class GestionCategoriasServicio {

    private final CategoriaRepositorio categorias;
    private final EquipoRepositorio equipos;

    public GestionCategoriasServicio(CategoriaRepositorio categorias,
                                     EquipoRepositorio equipos) {
        this.categorias = categorias;
        this.equipos = equipos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar(boolean soloActivas) {
        return categorias.listar(soloActivas).stream().map(CategoriaDto::de).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDto obtener(Long id) {
        return CategoriaDto.de(exigirCategoria(id));
    }

    @Transactional
    public CategoriaDto crear(GuardarCategoriaComando comando) {
        validarNombreUnico(comando.nombre(), null);

        Categoria categoria = Categoria.crear(
                comando.nombre(),
                comando.descripcion(),
                comando.activa() == null || comando.activa());

        Categoria guardada = categorias.guardar(categoria);

        return CategoriaDto.de(guardada);
    }

    @Transactional
    public CategoriaDto editar(GuardarCategoriaComando comando) {
        Categoria categoria = exigirCategoria(comando.id());
        validarNombreUnico(comando.nombre(), comando.id());

        categoria.actualizarDatos(comando.nombre(), comando.descripcion());
        Categoria guardada = categorias.guardar(categoria);

        return CategoriaDto.de(guardada);
    }

    /** RF-33: no se desactiva una categoria con bienes activos asociados. */
    @Transactional
    public CategoriaDto cambiarEstado(Long id, boolean activa) {
        Categoria categoria = exigirCategoria(id);
        if (activa) {
            categoria.activar();
        } else {
            categoria.desactivar(equipos.contarActivosEnCategoria(id));
        }
        Categoria guardada = categorias.guardar(categoria);

        return CategoriaDto.de(guardada);
    }

    private Categoria exigirCategoria(Long id) {
        return categorias.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de("la categoria", id));
    }

    private void validarNombreUnico(String nombre, Long idActual) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (!limpio.isEmpty() && categorias.existeNombre(limpio, idActual)) {
            throw new DatosInvalidosException("nombre", "Ya existe una categoria con ese nombre.");
        }
    }
}
