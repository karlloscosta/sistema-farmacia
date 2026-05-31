package farmacia.service;

import farmacia.model.*;
import farmacia.repository.ClienteRepository;
import farmacia.repository.MedicamentoRepository;
import farmacia.repository.VendaRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Responsável por registrar vendas e garantir a consistência do sistema.
 *
 * <p>Regras aplicadas:</p>
 * <ul>
 *   <li>Valida estoque de cada item antes de confirmar a venda.</li>
 *   <li>Cria e persiste o objeto {@link Venda} com todos os {@link ItemVenda}.</li>
 *   <li>Decrementa o estoque de cada medicamento vendido.</li>
 *   <li>Vendas registradas NÃO podem ser canceladas — devoluções são feitos
 *       via {@link EstoqueService#ajustarEstoque}.</li>
 *   <li>O cliente é opcional (venda avulsa).</li>
 * </ul>
 */
public class VendaService {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final VendaRepository vendaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ClienteRepository clienteRepository;
    private final EstoqueService estoqueService;

    public VendaService(VendaRepository vendaRepository,
                        MedicamentoRepository medicamentoRepository,
                        ClienteRepository clienteRepository,
                        EstoqueService estoqueService) {
        this.vendaRepository = vendaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.clienteRepository = clienteRepository;
        this.estoqueService = estoqueService;
    }

    // ------------------------------------------------------------------ //
    //  REGISTRO DE VENDA
    // ------------------------------------------------------------------ //

    /**
     * Registra uma venda completa.
     *
     * <p>O Controller monta a lista de itens (medicamentoId + quantidade)
     * e chama este método. Internamente:</p>
     * <ol>
     *   <li>Valida o estoque de todos os itens.</li>
     *   <li>Constrói os objetos {@link ItemVenda} capturando o preço atual.</li>
     *   <li>Persiste a {@link Venda}.</li>
     *   <li>Decrementa o estoque de cada medicamento.</li>
     * </ol>
     *
     * @param funcionario funcionário que realiza a venda (obrigatório)
     * @param clienteId   id do cliente (pode ser null para venda avulsa)
     * @param itensDto    lista de pares [medicamentoId, quantidade]
     * @return a {@link Venda} registrada
     * @throws IllegalArgumentException se funcionário for nulo ou a lista de itens estiver vazia
     * @throws IllegalStateException    se qualquer item tiver estoque insuficiente
     */
    public Venda registrarVenda(Funcionario funcionario,
                                 String clienteId,
                                 List<ItemVendaDto> itensDto) {

        // --- validações básicas ---
        if (funcionario == null) {
            throw new IllegalArgumentException("É necessário informar o funcionário responsável pela venda.");
        }
        if (itensDto == null || itensDto.isEmpty()) {
            throw new IllegalArgumentException("A venda deve conter ao menos um item.");
        }

        // --- valida estoque de todos os itens ANTES de debitar qualquer um ---
        for (ItemVendaDto dto : itensDto) {
            estoqueService.validarEstoqueParaVenda(dto.getMedicamentoId(), dto.getQuantidade());
        }

        // --- monta a Venda ---
        Venda venda = new Venda();
        venda.setId(UUID.randomUUID().toString());
        venda.setDataHora(LocalDateTime.now().format(FORMATO_DATA));
        venda.setFuncionario(funcionario);

        // cliente é opcional
        if (clienteId != null && !clienteId.isBlank()) {
            Cliente cliente = clienteRepository.buscarPorId(clienteId);
            if (cliente == null) {
                throw new IllegalArgumentException("Cliente não encontrado: id = " + clienteId);
            }
            venda.setCliente(cliente);
        }

        // --- adiciona os itens capturando o preço atual ---
        for (ItemVendaDto dto : itensDto) {
            Medicamento med = medicamentoRepository.buscarPorId(dto.getMedicamentoId());
            ItemVenda item = new ItemVenda();
            item.setId(UUID.randomUUID().toString());
            item.setMedicamento(med);
            item.setQuantidade(dto.getQuantidade());
            item.setPrecoUnitario(med.getPreco());  // captura o preço no momento da venda
            venda.adicionarItem(item);
        }

        // --- persiste a venda ---
        vendaRepository.salvar(venda);

        // --- decrementa estoque APÓS persistir ---
        for (ItemVendaDto dto : itensDto) {
            estoqueService.decrementarEstoque(dto.getMedicamentoId(), dto.getQuantidade());
        }

        return venda;
    }

    // ------------------------------------------------------------------ //
    //  CONSULTAS
    // ------------------------------------------------------------------ //

    /** Retorna todas as vendas registradas. */
    public List<Venda> listarVendas() {
        return vendaRepository.listarTodos();
    }

    /** Retorna as vendas de um funcionário específico. */
    public List<Venda> listarVendasPorFuncionario(String funcionarioId) {
        return vendaRepository.buscarPorFuncionario(funcionarioId);
    }

    /** Retorna as vendas associadas a um cliente específico. */
    public List<Venda> listarVendasPorCliente(String clienteId) {
        return vendaRepository.buscarPorCliente(clienteId);
    }

    // ------------------------------------------------------------------ //
    //  DTO interno
    // ------------------------------------------------------------------ //

    /**
     * Objeto de transferência simples para os itens da venda.
     * O Controller instancia um DTO por item e passa a lista para
     * {@link #registrarVenda}.
     */
    public static class ItemVendaDto {

        private final String medicamentoId;
        private final int quantidade;

        public ItemVendaDto(String medicamentoId, int quantidade) {
            if (medicamentoId == null || medicamentoId.isBlank()) {
                throw new IllegalArgumentException("medicamentoId não pode ser nulo ou vazio.");
            }
            if (quantidade <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
            }
            this.medicamentoId = medicamentoId;
            this.quantidade = quantidade;
        }

        public String getMedicamentoId() { return medicamentoId; }
        public int getQuantidade()        { return quantidade; }
    }
}