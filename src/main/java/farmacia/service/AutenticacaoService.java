package farmacia.service;

import farmacia.model.Usuario;
import farmacia.repository.UsuarioRepository;

/**
 * Responsável pela autenticação de usuários no sistema.
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>Busca o usuário pelo login no repositório.</li>
 *   <li>Compara a senha informada com a senha armazenada.</li>
 *   <li>Retorna o {@link Usuario} autenticado (Administrador ou Funcionario).</li>
 * </ul>
 */
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Autentica um usuário com login e senha.
     *
     * @param login login do usuário
     * @param senha senha em texto plano
     * @return o {@link Usuario} autenticado
     * @throws IllegalArgumentException se login não existir ou senha estiver errada
     */
    public Usuario autenticar(String login, String senha) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login não pode ser vazio.");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }

        Usuario usuario = usuarioRepository.buscarPorLogin(login);

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado: " + login);
        }
        if (!usuario.getSenha().equals(senha)) {
            throw new IllegalArgumentException("Senha incorreta.");
        }

        return usuario;
    }

    /**
     * Verifica se um login já está em uso (útil ao cadastrar novo usuário).
     *
     * @param login login a verificar
     * @return true se já existir
     */
    public boolean loginJaExiste(String login) {
        return usuarioRepository.buscarPorLogin(login) != null;
    }
}