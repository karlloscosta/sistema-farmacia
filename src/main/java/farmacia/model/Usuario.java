package farmacia.model;

public abstract class Usuario {

    private String id;
    private String nome;
    private String login;
    private String senha;

    public Usuario(String id, String nome, String login, String senha) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }

    public void setNome(String nome) { this.nome = nome; }
    public void setLogin(String login) { this.login = login; }
    public void setSenha(String senha) { this.senha = senha; }

    public abstract String getPerfil();

    @Override
    public String toString() {
        return "Usuario{id='" + id + "', nome='" + nome + "', login='" + login + "', perfil='" + getPerfil() + "'}";
    }
}