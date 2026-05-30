package farmacia.model;

public class Administrador extends Usuario {

    public Administrador(String id, String nome, String login, String senha) {
        super(id, nome, login, senha);
    }

    @Override
    public String getPerfil() {
        return "ADMINISTRADOR";
    }
}