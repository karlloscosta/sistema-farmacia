package farmacia.repository;

import farmacia.model.Cliente;
import farmacia.util.JsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persiste objetos {@link Cliente} no arquivo {@code dados/clientes.json}.
 */
public class ClienteRepository extends AbstractRepository<Cliente> {

    public ClienteRepository() {
        super("clientes.json");
    }

    @Override
    protected Map<String, Object> toMap(Cliente c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("nome", c.getNome());
        map.put("cpf", c.getCpf());
        map.put("telefone", c.getTelefone());
        map.put("email", c.getEmail());
        return map;
    }

    @Override
    protected Cliente fromMap(Map<String, Object> map) {
        Cliente c = new Cliente();
        c.setId(JsonUtil.getString(map, "id"));
        c.setNome(JsonUtil.getString(map, "nome"));
        c.setCpf(JsonUtil.getString(map, "cpf"));
        c.setTelefone(JsonUtil.getString(map, "telefone"));
        c.setEmail(JsonUtil.getString(map, "email"));
        return c;
    }

    @Override
    protected String getId(Cliente objeto) {
        return objeto.getId();
    }

    /** Busca cliente pelo CPF. Retorna null se não encontrado. */
    public Cliente buscarPorCpf(String cpf) {
        for (Cliente c : listarTodos()) {
            if (cpf.equals(c.getCpf())) return c;
        }
        return null;
    }
}