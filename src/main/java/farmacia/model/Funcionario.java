package farmacia.model;

public class Funcionario extends Usuario {

    public Funcionario(String id, String nome, String login, String senha) {
        super(id, nome, login, senha);
    }

    @Override
    public String getPerfil() {
        return "FUNCIONARIO";
    }
}