public class Token {
    public enum Type {
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        ASSIGN,
        EQUALS,

        IDENTIFIER,
        INTEGER,

        SEMICOLON,

        OPEN_BRACKET,
        CLOSE_BRACKET,

        RETURN,

        END_OF_PROGRAM
    }

    private Type type;

    private String value;

    public Token( Type type ) {
        this.type = type;
    }

    public Token( Type type, String value ) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
