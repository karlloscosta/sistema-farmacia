package farmacia.repository;

import farmacia.model.Categoria;
import farmacia.util.JsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persiste objetos {@link Categoria} no arquivo {@code dados/categorias.json}.
 */
public class CategoriaRepository extends AbstractRepository<Categoria> {

    public CategoriaRepository() {
        super("categorias.json");
    }

    @Override
    protected Map<String, Object> toMap(Categoria c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("nome", c.getNome());
        map.put("descricao", c.getDescricao());
        return map;
    }

    @Override
    protected Categoria fromMap(Map<String, Object> map) {
        Categoria c = new Categoria();
        c.setId(JsonUtil.getString(map, "id"));
        c.setNome(JsonUtil.getString(map, "nome"));
        c.setDescricao(JsonUtil.getString(map, "descricao"));
        return c;
    }

    @Override
    protected String getId(Categoria objeto) {
        return objeto.getId();
    }
}