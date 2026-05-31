package farmacia.repository;

import farmacia.model.Categoria;
import farmacia.model.Fornecedor;
import farmacia.model.Medicamento;
import farmacia.util.JsonUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persiste objetos {@link Medicamento} no arquivo {@code dados/medicamentos.json}.
 *
 * <p>Categoria e Fornecedor são gravados apenas pelo id para evitar
 * duplicação de dados. Ao carregar, os objetos são reconstruídos com os
 * repositories correspondentes.</p>
 */
public class MedicamentoRepository extends AbstractRepository<Medicamento> {

    private final CategoriaRepository categoriaRepo;
    private final FornecedorRepository fornecedorRepo;

    public MedicamentoRepository(CategoriaRepository categoriaRepo,
                                  FornecedorRepository fornecedorRepo) {
        super("medicamentos.json");
        this.categoriaRepo = categoriaRepo;
        this.fornecedorRepo = fornecedorRepo;
    }

    @Override
    protected Map<String, Object> toMap(Medicamento m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("nome", m.getNome());
        map.put("descricao", m.getDescricao());
        map.put("preco", m.getPreco());
        map.put("quantidadeEstoque", m.getQuantidadeEstoque());
        map.put("quantidadeMinima", m.getQuantidadeMinima());
        map.put("dataValidade", m.getDataValidade());   // String ISO: "2025-12-31"
        map.put("ativo", m.isAtivo());
        map.put("categoriaId", m.getCategoria() != null ? m.getCategoria().getId() : null);
        map.put("fornecedorId", m.getFornecedor() != null ? m.getFornecedor().getId() : null);
        return map;
    }

    @Override
    protected Medicamento fromMap(Map<String, Object> map) {
        Medicamento m = new Medicamento();
        m.setId(JsonUtil.getString(map, "id"));
        m.setNome(JsonUtil.getString(map, "nome"));
        m.setDescricao(JsonUtil.getString(map, "descricao"));
        m.setPreco(JsonUtil.getDouble(map, "preco"));
        m.setQuantidadeEstoque(JsonUtil.getInt(map, "quantidadeEstoque"));
        m.setQuantidadeMinima(JsonUtil.getInt(map, "quantidadeMinima"));
        m.setDataValidade(JsonUtil.getString(map, "dataValidade"));
        m.setAtivo(JsonUtil.getBoolean(map, "ativo"));

        String categoriaId = JsonUtil.getString(map, "categoriaId");
        if (categoriaId != null) {
            Categoria cat = categoriaRepo.buscarPorId(categoriaId);
            m.setCategoria(cat);
        }

        String fornecedorId = JsonUtil.getString(map, "fornecedorId");
        if (fornecedorId != null) {
            Fornecedor forn = fornecedorRepo.buscarPorId(fornecedorId);
            m.setFornecedor(forn);
        }

        return m;
    }

    @Override
    protected String getId(Medicamento objeto) {
        return objeto.getId();
    }

    /** Retorna apenas os medicamentos ativos. */
    public List<Medicamento> listarAtivos() {
        return listarTodos().stream()
                .filter(Medicamento::isAtivo)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Busca medicamento pelo nome (busca parcial, ignora maiúsculas). */
    public List<Medicamento> buscarPorNome(String nome) {
        String nomeMin = nome.toLowerCase();
        return listarTodos().stream()
                .filter(m -> m.getNome().toLowerCase().contains(nomeMin))
                .collect(java.util.stream.Collectors.toList());
    }
}