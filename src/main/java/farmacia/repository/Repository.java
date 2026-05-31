package farmacia.repository;

import java.util.List;

/**
 * Interface genérica para operações de persistência em arquivo JSON.
 *
 * @param <T> tipo da entidade gerenciada
 */
public interface Repository<T> {

    /** Persiste um novo objeto no arquivo JSON. */
    void salvar(T objeto);

    /** Busca um objeto pelo seu id. Retorna null se não encontrado. */
    T buscarPorId(String id);

    /** Retorna todos os objetos persistidos. */
    List<T> listarTodos();

    /** Atualiza um objeto já existente (identifica pelo id). */
    void atualizar(T objeto);

    /** Remove o objeto com o id informado. */
    void remover(String id);
}