public class Token {
    public enum Type {
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        POWER,

        ASSIGN,
        EQUALS,

        IDENTIFIER,

        INTEGER_TYPE,
        DOUBLE_TYPE,
        VOID_TYPE,

        INTEGER_VALUE,
        DOUBLE_VALUE,

        COMMA,
        SEMICOLON,

        OPEN_BRACKET,
        CLOSE_BRACKET,

        OPEN_BRACE,
        CLOSE_BRACE,

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

    public boolean hasType( Type type ) {
        return this.type.equals( type );
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Token token = (Token) o;

        if ( type != token.type ) return false;
        if ( value != null ? !value.equals( token.value ) : token.value != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if ( value == null ) {
            return type.toString();
        }

        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
