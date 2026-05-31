package farmacia.repository;

import farmacia.util.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Implementação base para todos os Repositories.
 * Cuida de leitura e escrita no arquivo JSON, deixando
 * apenas a conversão objeto<->Map para as subclasses.
 *
 * @param <T> tipo da entidade
 */
public abstract class AbstractRepository<T> implements Repository<T> {

    /** Pasta onde todos os arquivos JSON são armazenados. */
    private static final String PASTA_DADOS = "dados";

    private final Path arquivoJson;

    protected AbstractRepository(String nomeArquivo) {
        Path pasta = Paths.get(PASTA_DADOS);
        try {
            Files.createDirectories(pasta);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta 'dados'.", e);
        }
        this.arquivoJson = pasta.resolve(nomeArquivo);
    }

    // ------------------------------------------------------------------ //
    //  CONTRATO DAS SUBCLASSES
    // ------------------------------------------------------------------ //

    /** Serializa o objeto para um Map que será gravado no JSON. */
    protected abstract Map<String, Object> toMap(T objeto);

    /** Desserializa um Map lido do JSON para o objeto do domínio. */
    protected abstract T fromMap(Map<String, Object> map);

    /** Retorna o id único do objeto (campo "id" por padrão). */
    protected abstract String getId(T objeto);

    // ------------------------------------------------------------------ //
    //  IMPLEMENTAÇÃO DA INTERFACE Repository<T>
    // ------------------------------------------------------------------ //

    @Override
    public void salvar(T objeto) {
        List<Map<String, Object>> lista = lerArquivo();
        lista.add(toMap(objeto));
        escreverArquivo(lista);
    }

    @Override
    public T buscarPorId(String id) {
        for (Map<String, Object> map : lerArquivo()) {
            if (id.equals(JsonUtil.getString(map, "id"))) {
                return fromMap(map);
            }
        }
        return null;
    }

    @Override
    public List<T> listarTodos() {
        List<T> resultado = new ArrayList<>();
        for (Map<String, Object> map : lerArquivo()) {
            resultado.add(fromMap(map));
        }
        return resultado;
    }

    @Override
    public void atualizar(T objeto) {
        List<Map<String, Object>> lista = lerArquivo();
        String id = getId(objeto);
        boolean encontrado = false;
        for (int i = 0; i < lista.size(); i++) {
            if (id.equals(JsonUtil.getString(lista.get(i), "id"))) {
                lista.set(i, toMap(objeto));
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            throw new IllegalArgumentException("Registro com id '" + id + "' não encontrado para atualização.");
        }
        escreverArquivo(lista);
    }

    @Override
    public void remover(String id) {
        List<Map<String, Object>> lista = lerArquivo();
        boolean removido = lista.removeIf(map -> id.equals(JsonUtil.getString(map, "id")));
        if (!removido) {
            throw new IllegalArgumentException("Registro com id '" + id + "' não encontrado para remoção.");
        }
        escreverArquivo(lista);
    }

    // ------------------------------------------------------------------ //
    //  LEITURA E ESCRITA NO ARQUIVO
    // ------------------------------------------------------------------ //

    /** Lê o arquivo JSON e retorna a lista de maps. */
    protected List<Map<String, Object>> lerArquivo() {
        if (!Files.exists(arquivoJson)) return new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(arquivoJson, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String linha;
            while ((linha = reader.readLine()) != null) sb.append(linha);
            String conteudo = sb.toString().trim();
            if (conteudo.isEmpty()) return new ArrayList<>();
            return JsonUtil.parseObjectArray(conteudo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo: " + arquivoJson, e);
        }
    }

    /** Grava a lista de maps no arquivo JSON (sobrescreve). */
    private void escreverArquivo(List<Map<String, Object>> lista) {
        try (BufferedWriter writer = Files.newBufferedWriter(arquivoJson, StandardCharsets.UTF_8)) {
            writer.write(serializarLista(lista));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever arquivo: " + arquivoJson, e);
        }
    }

    /** Gera o JSON com indentação simples para facilitar leitura. */
    private String serializarLista(List<Map<String, Object>> lista) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < lista.size(); i++) {
            sb.append("  ").append(JsonUtil.toJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}