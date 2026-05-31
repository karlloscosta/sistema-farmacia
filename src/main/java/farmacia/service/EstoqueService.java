package farmacia.service;

import farmacia.model.Administrador;
import farmacia.model.Medicamento;
import farmacia.model.Usuario;
import farmacia.repository.MedicamentoRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Regras de negócio relacionadas ao estoque de medicamentos.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Verificar disponibilidade antes de uma venda.</li>
 *   <li>Bloquear venda quando estoque está zerado.</li>
 *   <li>Gerar alertas de estoque baixo e validade próxima.</li>
 *   <li>Permitir ajuste manual de estoque (somente Administrador).</li>
 * </ul>
 */
public class EstoqueService {

    private final MedicamentoRepository medicamentoRepository;

    public EstoqueService(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    // ------------------------------------------------------------------ //
    //  VERIFICAÇÃO DE DISPONIBILIDADE
    // ------------------------------------------------------------------ //

    /**
     * Verifica se há estoque suficiente para a quantidade solicitada.
     *
     * @param medicamentoId id do medicamento
     * @param quantidade    quantidade desejada
     * @return true se disponível
     * @throws IllegalArgumentException se o medicamento não existir
     */
    public boolean verificarDisponibilidade(String medicamentoId, int quantidade) {
        Medicamento med = buscarOuLancar(medicamentoId);
        return med.getQuantidadeEstoque() >= quantidade;
    }

    /**
     * Valida se a venda pode ser realizada para a quantidade informada.
     * Lança exceção descritiva se não puder.
     *
     * @throws IllegalStateException se estoque for insuficiente ou zerado
     */
    public void validarEstoqueParaVenda(String medicamentoId, int quantidade) {
        Medicamento med = buscarOuLancar(medicamentoId);

        if (med.isEstoqueZerado()) {
            throw new IllegalStateException(
                    "Venda bloqueada: o medicamento '" + med.getNome() + "' está com estoque zerado.");
        }
        if (med.getQuantidadeEstoque() < quantidade) {
            throw new IllegalStateException(
                    "Estoque insuficiente para '" + med.getNome() + "'. " +
                    "Disponível: " + med.getQuantidadeEstoque() + ", solicitado: " + quantidade + ".");
        }
    }

    // ------------------------------------------------------------------ //
    //  ALERTAS
    // ------------------------------------------------------------------ //

    /**
     * Retorna medicamentos com estoque abaixo do mínimo configurado.
     * Deve ser chamado ao iniciar o sistema para exibir alertas.
     */
    public List<Medicamento> listarEstoqueBaixo() {
        return medicamentoRepository.listarAtivos().stream()
                .filter(Medicamento::isEstoqueBaixo)
                .collect(Collectors.toList());
    }

    /**
     * Retorna medicamentos com validade igual ou inferior a 30 dias.
     * Deve ser chamado ao iniciar o sistema para exibir alertas.
     */
    public List<Medicamento> listarValidadeProxima() {
        return medicamentoRepository.listarAtivos().stream()
                .filter(Medicamento::isValidadeProxima)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  AJUSTE MANUAL (somente Administrador)
    // ------------------------------------------------------------------ //

    /**
     * Ajusta o estoque de um medicamento manualmente.
     * Utilizado para registrar devoluções ou correções de inventário.
     *
     * <p>Somente um {@link Administrador} pode executar esta operação.</p>
     *
     * @param medicamentoId id do medicamento
     * @param quantidade    valor a adicionar (positivo) ou subtrair (negativo)
     * @param solicitante   usuário que solicita o ajuste
     * @throws SecurityException     se o solicitante não for Administrador
     * @throws IllegalStateException se o ajuste resultar em estoque negativo
     */
    public void ajustarEstoque(String medicamentoId, int quantidade, Usuario solicitante) {
        if (!(solicitante instanceof Administrador)) {
            throw new SecurityException(
                    "Acesso negado: apenas Administradores podem ajustar o estoque manualmente.");
        }

        Medicamento med = buscarOuLancar(medicamentoId);
        int novoEstoque = med.getQuantidadeEstoque() + quantidade;

        if (novoEstoque < 0) {
            throw new IllegalStateException(
                    "Ajuste inválido: o estoque de '" + med.getNome() +
                    "' ficaria negativo (" + novoEstoque + ").");
        }

        med.setQuantidadeEstoque(novoEstoque);
        medicamentoRepository.atualizar(med);
    }

    // ------------------------------------------------------------------ //
    //  ATUALIZAÇÃO INTERNA (usada pelo VendaService)
    // ------------------------------------------------------------------ //

    /**
     * Decrementa o estoque após uma venda confirmada.
     * Chamado internamente pelo {@link VendaService} — não exposto ao Controller.
     *
     * @param medicamentoId id do medicamento
     * @param quantidade    quantidade vendida
     */
    void decrementarEstoque(String medicamentoId, int quantidade) {
        Medicamento med = buscarOuLancar(medicamentoId);
        med.setQuantidadeEstoque(med.getQuantidadeEstoque() - quantidade);
        medicamentoRepository.atualizar(med);
    }

    // ------------------------------------------------------------------ //
    //  AUXILIAR
    // ------------------------------------------------------------------ //

    private Medicamento buscarOuLancar(String medicamentoId) {
        Medicamento med = medicamentoRepository.buscarPorId(medicamentoId);
        if (med == null) {
            throw new IllegalArgumentException("Medicamento não encontrado: id = " + medicamentoId);
        }
        return med;
    }
}