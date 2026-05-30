package farmacia.model;

public class Cliente {

    private String id;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;

    public Cliente(String id, String nome, String cpf, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }

    public void setNome(String nome) { this.nome = nome; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Cliente{id='" + id + "', nome='" + nome + "', cpf='" + cpf + "'}";
    }
}