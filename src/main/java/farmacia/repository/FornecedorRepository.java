package farmacia.repository;

import farmacia.model.Fornecedor;
import farmacia.util.JsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persiste objetos {@link Fornecedor} no arquivo {@code dados/fornecedores.json}.
 */
public class FornecedorRepository extends AbstractRepository<Fornecedor> {

    public FornecedorRepository() {
        super("fornecedores.json");
    }

    @Override
    protected Map<String, Object> toMap(Fornecedor f) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", f.getId());
        map.put("nome", f.getNome());
        map.put("cnpj", f.getCnpj());
        map.put("telefone", f.getTelefone());
        map.put("email", f.getEmail());
        return map;
    }

    @Override
    protected Fornecedor fromMap(Map<String, Object> map) {
        Fornecedor f = new Fornecedor();
        f.setId(JsonUtil.getString(map, "id"));
        f.setNome(JsonUtil.getString(map, "nome"));
        f.setCnpj(JsonUtil.getString(map, "cnpj"));
        f.setTelefone(JsonUtil.getString(map, "telefone"));
        f.setEmail(JsonUtil.getString(map, "email"));
        return f;
    }

    @Override
    protected String getId(Fornecedor objeto) {
        return objeto.getId();
    }
}