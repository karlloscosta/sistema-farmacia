package farmacia.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venda {

    private String id;
    private LocalDateTime dataHora;
    private Funcionario funcionario;
    private Cliente cliente;
    private List<ItemVenda> itens;

    public Venda(String id, Funcionario funcionario, Cliente cliente) {
        this.id = id;
        this.dataHora = LocalDateTime.now();
        this.funcionario = funcionario;
        this.cliente = cliente;
        this.itens = new ArrayList<>();
    }

    public String getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public Funcionario getFuncionario() { return funcionario; }
    public Cliente getCliente() { return cliente; }
    public List<ItemVenda> getItens() { return itens; }

    public void adicionarItem(ItemVenda item) {
        itens.add(item);
    }

    public double calcularTotal() {
        return itens.stream()
                .mapToDouble(ItemVenda::getSubtotal)
                .sum();
    }

    @Override
    public String toString() {
        return "Venda{id='" + id + "', funcionario='" + funcionario.getNome() +
               "', cliente='" + (cliente != null ? cliente.getNome() : "Avulso") +
               "', total=" + calcularTotal() + "}";
    }
}