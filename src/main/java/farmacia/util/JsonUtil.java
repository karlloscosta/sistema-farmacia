package farmacia.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser e serializador JSON minimalista, sem dependências externas.
 *
 * Suporta:
 *   - Objetos  { "chave": valor, ... }
 *   - Arrays   [ valor, valor, ... ]
 *   - Strings  "texto"
 *   - Números  inteiros e decimais
 *   - Booleanos true / false
 *   - null
 *
 * Uso:
 *   Map<String,Object> obj = JsonUtil.parseObject(jsonString);
 *   String json = JsonUtil.toJson(objeto);
 */
public final class JsonUtil {

    private JsonUtil() {}

    // ------------------------------------------------------------------ //
    //  PARSE
    // ------------------------------------------------------------------ //

    /**
     * Converte uma String JSON de objeto em Map<String, Object>.
     * Arrays internos viram List<Object>.
     * Objetos internos viram Map<String, Object>.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        Object parsed = parseValue(json.trim(), new int[]{0});
        if (parsed instanceof Map) return (Map<String, Object>) parsed;
        return new LinkedHashMap<>();
    }

    /**
     * Converte uma String JSON de array em List<Object>.
     */
    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        Object parsed = parseValue(json.trim(), new int[]{0});
        if (parsed instanceof List) return (List<Object>) parsed;
        return new ArrayList<>();
    }

    /** Converte List<Map> diretamente (atalho para listarTodos). */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseObjectArray(String json) {
        List<Object> raw = parseArray(json);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) result.add((Map<String, Object>) o);
        }
        return result;
    }

    // ------------------------------------------------------------------ //
    //  SERIALIZE
    // ------------------------------------------------------------------ //

    /** Serializa qualquer valor Java suportado em String JSON. */
    public static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + escapeString((String) value) + "\"";
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        if (value instanceof Map) return mapToJson((Map<?, ?>) value);
        if (value instanceof List) return listToJson((List<?>) value);
        // fallback: tratar como string
        return "\"" + escapeString(value.toString()) + "\"";
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeString(entry.getKey().toString())).append("\":");
            sb.append(toJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(",");
            sb.append(toJson(item));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ------------------------------------------------------------------ //
    //  HELPERS de acesso
    // ------------------------------------------------------------------ //

    public static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    public static int getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return 0; }
    }

    public static double getDouble(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return 0.0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0.0; }
    }

    public static boolean getBoolean(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(v.toString());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getObject(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Map) return (Map<String, Object>) v;
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getObjectList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (!(v instanceof List)) return new ArrayList<>();
        List<?> raw = (List<?>) v;
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object o : raw) {
            if (o instanceof Map) result.add((Map<String, Object>) o);
        }
        return result;
    }

    // ------------------------------------------------------------------ //
    //  PARSER INTERNO
    // ------------------------------------------------------------------ //

    /**
     * Analisa recursivamente um valor JSON a partir da posição pos[0].
     * Avança pos[0] até o fim do valor lido.
     */
    private static Object parseValue(String json, int[] pos) {
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) return null;

        char c = json.charAt(pos[0]);

        if (c == '{') return parseObjectInternal(json, pos);
        if (c == '[') return parseArrayInternal(json, pos);
        if (c == '"') return parseString(json, pos);
        if (c == 't') { pos[0] += 4; return Boolean.TRUE; }
        if (c == 'f') { pos[0] += 5; return Boolean.FALSE; }
        if (c == 'n') { pos[0] += 4; return null; }
        return parseNumber(json, pos);
    }

    private static Map<String, Object> parseObjectInternal(String json, int[] pos) {
        Map<String, Object> map = new LinkedHashMap<>();
        pos[0]++; // consome '{'
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == '}') { pos[0]++; return map; }

        while (pos[0] < json.length()) {
            skipWhitespace(json, pos);
            String key = parseString(json, pos);
            skipWhitespace(json, pos);
            pos[0]++; // consome ':'
            Object value = parseValue(json, pos);
            map.put(key, value);
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) break;
            char next = json.charAt(pos[0]);
            if (next == '}') { pos[0]++; break; }
            if (next == ',') pos[0]++;
        }
        return map;
    }

    private static List<Object> parseArrayInternal(String json, int[] pos) {
        List<Object> list = new ArrayList<>();
        pos[0]++; // consome '['
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == ']') { pos[0]++; return list; }

        while (pos[0] < json.length()) {
            list.add(parseValue(json, pos));
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) break;
            char next = json.charAt(pos[0]);
            if (next == ']') { pos[0]++; break; }
            if (next == ',') pos[0]++;
        }
        return list;
    }

    private static String parseString(String json, int[] pos) {
        pos[0]++; // consome '"'
        StringBuilder sb = new StringBuilder();
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == '"') { pos[0]++; break; }
            if (c == '\\') {
                pos[0]++;
                if (pos[0] >= json.length()) break;
                char esc = json.charAt(pos[0]);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
            pos[0]++;
        }
        return sb.toString();
    }

    private static Number parseNumber(String json, int[] pos) {
        int start = pos[0];
        boolean isDecimal = false;
        if (pos[0] < json.length() && json.charAt(pos[0]) == '-') pos[0]++;
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == '.' || c == 'e' || c == 'E') { isDecimal = true; pos[0]++; }
            else if (Character.isDigit(c) || c == '+' || c == '-') pos[0]++;
            else break;
        }
        String num = json.substring(start, pos[0]);
        try {
            if (isDecimal) return Double.parseDouble(num);
            long l = Long.parseLong(num);
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
            return l;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void skipWhitespace(String json, int[] pos) {
        while (pos[0] < json.length() && Character.isWhitespace(json.charAt(pos[0]))) {
            pos[0]++;
        }
    }
}