package farmacia.repository;

import farmacia.model.*;
import farmacia.util.JsonUtil;

import java.util.*;

/**
 * Persiste objetos {@link Venda} no arquivo {@code dados/vendas.json}.
 *
 * <p>Estrutura JSON de uma venda:</p>
 * <pre>
 * {
 *   "id": "...",
 *   "dataHora": "2025-06-01T14:30:00",
 *   "funcionarioId": "...",
 *   "clienteId": "...",          // pode ser null em venda avulsa
 *   "itens": [
 *     {
 *       "id": "...",
 *       "medicamentoId": "...",
 *       "quantidade": 2,
 *       "precoUnitario": 12.50
 *     }
 *   ]
 * }
 * </pre>
 */
public class VendaRepository extends AbstractRepository<Venda> {

    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final MedicamentoRepository medicamentoRepo;

    public VendaRepository(UsuarioRepository usuarioRepo,
                            ClienteRepository clienteRepo,
                            MedicamentoRepository medicamentoRepo) {
        super("vendas.json");
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.medicamentoRepo = medicamentoRepo;
    }

    // ------------------------------------------------------------------ //
    //  SERIALIZAÇÃO
    // ------------------------------------------------------------------ //

    @Override
    protected Map<String, Object> toMap(Venda v) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", v.getId());
        map.put("dataHora", v.getDataHora());   // String ISO
        map.put("funcionarioId", v.getFuncionario() != null ? v.getFuncionario().getId() : null);
        map.put("clienteId", v.getCliente() != null ? v.getCliente().getId() : null);
        map.put("itens", serializarItens(v.getItens()));
        return map;
    }

    private List<Map<String, Object>> serializarItens(List<ItemVenda> itens) {
        List<Map<String, Object>> lista = new ArrayList<>();
        if (itens == null) return lista;
        for (ItemVenda item : itens) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", item.getId());
            m.put("medicamentoId", item.getMedicamento() != null ? item.getMedicamento().getId() : null);
            m.put("quantidade", item.getQuantidade());
            m.put("precoUnitario", item.getPrecoUnitario());
            lista.add(m);
        }
        return lista;
    }

    // ------------------------------------------------------------------ //
    //  DESSERIALIZAÇÃO
    // ------------------------------------------------------------------ //

    @Override
    protected Venda fromMap(Map<String, Object> map) {
        Venda v = new Venda();
        v.setId(JsonUtil.getString(map, "id"));
        v.setDataHora(JsonUtil.getString(map, "dataHora"));

        String funcId = JsonUtil.getString(map, "funcionarioId");
        if (funcId != null) {
            Usuario u = usuarioRepo.buscarPorId(funcId);
            if (u instanceof Funcionario) v.setFuncionario((Funcionario) u);
        }

        String clienteId = JsonUtil.getString(map, "clienteId");
        if (clienteId != null) {
            v.setCliente(clienteRepo.buscarPorId(clienteId));
        }

        List<Map<String, Object>> itensMap = JsonUtil.getObjectList(map, "itens");
        for (Map<String, Object> itemMap : itensMap) {
            ItemVenda item = desserializarItem(itemMap);
            if (item != null) v.adicionarItem(item);
        }

        return v;
    }

    private ItemVenda desserializarItem(Map<String, Object> map) {
        String medId = JsonUtil.getString(map, "medicamentoId");
        if (medId == null) return null;
        Medicamento med = medicamentoRepo.buscarPorId(medId);
        if (med == null) return null;

        ItemVenda item = new ItemVenda();
        item.setId(JsonUtil.getString(map, "id"));
        item.setMedicamento(med);
        item.setQuantidade(JsonUtil.getInt(map, "quantidade"));
        item.setPrecoUnitario(JsonUtil.getDouble(map, "precoUnitario"));
        return item;
    }

    @Override
    protected String getId(Venda objeto) {
        return objeto.getId();
    }

    // ------------------------------------------------------------------ //
    //  CONSULTAS EXTRAS
    // ------------------------------------------------------------------ //

    /** Retorna todas as vendas de um determinado funcionário. */
    public List<Venda> buscarPorFuncionario(String funcionarioId) {
        List<Venda> resultado = new ArrayList<>();
        for (Venda v : listarTodos()) {
            if (v.getFuncionario() != null && funcionarioId.equals(v.getFuncionario().getId())) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    /** Retorna todas as vendas de um determinado cliente. */
    public List<Venda> buscarPorCliente(String clienteId) {
        List<Venda> resultado = new ArrayList<>();
        for (Venda v : listarTodos()) {
            if (v.getCliente() != null && clienteId.equals(v.getCliente().getId())) {
                resultado.add(v);
            }
        }
        return resultado;
    }
}