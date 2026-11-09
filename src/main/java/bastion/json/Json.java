package bastion.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal, dependency-free JSON reader/writer.
 *
 * <p>Parsing produces plain Java containers: {@link LinkedHashMap} for objects,
 * {@link List} for arrays, {@link String}, {@link Double} for numbers,
 * {@link Boolean} and {@code null}. This keeps the rest of Bastion free of
 * third-party JSON libraries while remaining predictable and easy to test.</p>
 */
public final class Json {

    private Json() {}

    public static Object parse(String text) {
        if (text == null) throw new JsonException("cannot parse null");
        Parser p = new Parser(text);
        Object v = p.parse();
        p.skipWs();
        if (p.i != p.s.length()) throw new JsonException("trailing content at " + p.i);
        return v;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object o = parse(text);
        if (!(o instanceof Map)) throw new JsonException("expected JSON object");
        return (Map<String, Object>) o;
    }

    public static String stringify(Object value) {
        return new Writer().write(value);
    }

    public static final class JsonException extends RuntimeException {
        public JsonException(String message) { super(message); }
    }

    static final class Parser {
        final String s;
        int i;

        Parser(String s) { this.s = s; this.i = skipWs(); }

        int skipWs() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
            return i;
        }

        Object parse() {
            if (i >= s.length()) throw new JsonException("unexpected end of input");
            char c = s.charAt(i);
            switch (c) {
                case '{': return object();
                case '[': return array();
                case '"': return string();
                case 't': case 'f': return bool();
                case 'n': return nullValue();
                default: return number();
            }
        }

        Map<String, Object> object() {
            expect('{');
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            i = skipWs();
            if (peek() == '}') { i++; return m; }
            while (true) {
                i = skipWs();
                String key = string();
                i = skipWs();
                expect(':');
                i = skipWs();
                m.put(key, parse());
                i = skipWs();
                char c = peek();
                if (c == ',') { i++; continue; }
                if (c == '}') { i++; break; }
                throw new JsonException("expected ',' or '}' at " + i);
            }
            return m;
        }

        List<Object> array() {
            expect('[');
            List<Object> list = new ArrayList<>();
            i = skipWs();
            if (peek() == ']') { i++; return list; }
            while (true) {
                i = skipWs();
                list.add(parse());
                i = skipWs();
                char c = peek();
                if (c == ',') { i++; continue; }
                if (c == ']') { i++; break; }
                throw new JsonException("expected ',' or ']' at " + i);
            }
            return list;
        }

        char peek() {
            if (i >= s.length()) throw new JsonException("unexpected end of input");
            return s.charAt(i);
        }

        void expect(char c) {
            if (peek() != c) throw new JsonException("expected '" + c + "' at " + i);
            i++;
        }

        String string() {
            expect('"');
            StringBuilder b = new StringBuilder();
            while (true) {
                if (i >= s.length()) throw new JsonException("unterminated string");
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case '"': b.append('"'); break;
                        case '\\': b.append('\\'); break;
                        case '/': b.append('/'); break;
                        case 'n': b.append('\n'); break;
                        case 't': b.append('\t'); break;
                        case 'r': b.append('\r'); break;
                        case 'b': b.append('\b'); break;
                        case 'f': b.append('\f'); break;
                        case 'u':
                            String hex = s.substring(i, i + 4);
                            i += 4;
                            b.append((char) Integer.parseInt(hex, 16));
                            break;
                        default: throw new JsonException("invalid escape \\" + e);
                    }
                } else {
                    b.append(c);
                }
            }
            return b.toString();
        }

        Object number() {
            int start = i;
            while (i < s.length() && "0123456789+-.eE".indexOf(s.charAt(i)) >= 0) i++;
            String raw = s.substring(start, i);
            if (raw.isEmpty()) throw new JsonException("expected value at " + start);
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                throw new JsonException("invalid number '" + raw + "'");
            }
        }

        Boolean bool() {
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            throw new JsonException("invalid literal at " + i);
        }

        Object nullValue() {
            if (s.startsWith("null", i)) { i += 4; return null; }
            throw new JsonException("invalid literal at " + i);
        }
    }

    static final class Writer {
        final StringBuilder b = new StringBuilder();

        String write(Object o) {
            value(o);
            return b.toString();
        }

        void value(Object o) {
            if (o == null) { b.append("null"); return; }
            if (o instanceof String) { string((String) o); return; }
            if (o instanceof Boolean) { b.append(o.toString()); return; }
            if (o instanceof Number) { b.append(o.toString()); return; }
            if (o instanceof Map) { object((Map<?, ?>) o); return; }
            if (o instanceof Iterable) { array((Iterable<?>) o); return; }
            string(o.toString());
        }

        void string(String s) {
            b.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"': b.append("\\\""); break;
                    case '\\': b.append("\\\\"); break;
                    case '\n': b.append("\\n"); break;
                    case '\t': b.append("\\t"); break;
                    case '\r': b.append("\\r"); break;
                    case '\b': b.append("\\b"); break;
                    case '\f': b.append("\\f"); break;
                    default:
                        if (c < ' ') b.append(String.format("\\u%04x", (int) c));
                        else b.append(c);
                }
            }
            b.append('"');
        }

        void object(Map<?, ?> m) {
            b.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) b.append(',');
                first = false;
                string(String.valueOf(e.getKey()));
                b.append(':');
                value(e.getValue());
            }
            b.append('}');
        }

        void array(Iterable<?> it) {
            b.append('[');
            boolean first = true;
            for (Object o : it) {
                if (!first) b.append(',');
                first = false;
                value(o);
            }
            b.append(']');
        }
    }
}
