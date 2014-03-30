import java.util.HashMap;
import java.util.Map;

public class Tokenizer implements TokenizerInterface {
    private enum State {
        READY,
        INTEGER,
        DOUBLE,
        WORD
    }

    private State state;

    private BufferInterface buffer;

    private Map<Token.Type, Token> readyTokens = new HashMap<>();

    {
        readyTokens.put(Token.Type.PLUS, new Token( Token.Type.PLUS ) );
        readyTokens.put(Token.Type.MINUS, new Token( Token.Type.MINUS ) );
        readyTokens.put(Token.Type.MULTIPLY, new Token( Token.Type.MULTIPLY ) );
        readyTokens.put(Token.Type.DIVIDE, new Token( Token.Type.DIVIDE ) );

        readyTokens.put( Token.Type.OPEN_BRACKET, new Token( Token.Type.OPEN_BRACKET ) );
        readyTokens.put( Token.Type.CLOSE_BRACKET, new Token( Token.Type.CLOSE_BRACKET ) );

        readyTokens.put( Token.Type.OPEN_BRACE, new Token( Token.Type.OPEN_BRACE ) );
        readyTokens.put( Token.Type.CLOSE_BRACE, new Token( Token.Type.CLOSE_BRACE ) );

        readyTokens.put( Token.Type.ASSIGN, new Token( Token.Type.ASSIGN ) );

        readyTokens.put( Token.Type.EQUALS, new Token( Token.Type.EQUALS ) );

        readyTokens.put( Token.Type.SEMICOLON, new Token( Token.Type.SEMICOLON ) );

        readyTokens.put( Token.Type.INTEGER_TYPE, new Token( Token.Type.INTEGER_TYPE ) );
        readyTokens.put( Token.Type.DOUBLE_TYPE, new Token( Token.Type.DOUBLE_TYPE ) );
        readyTokens.put( Token.Type.VOID_TYPE, new Token( Token.Type.VOID_TYPE ) );

        readyTokens.put( Token.Type.RETURN, new Token( Token.Type.RETURN ) );

        readyTokens.put( Token.Type.END_OF_PROGRAM, new Token( Token.Type.END_OF_PROGRAM ) );
    }

    private StringBuilder value = new StringBuilder();

    private Map<String, Token.Type> keywords = new HashMap<>();

    public Tokenizer( BufferInterface buffer ) {
        this.buffer = buffer;

        keywords.put( "return", Token.Type.RETURN );
        keywords.put( "int", Token.Type.INTEGER_TYPE );
        keywords.put( "double", Token.Type.DOUBLE_TYPE );
        keywords.put( "void", Token.Type.VOID_TYPE );
    }

    @Override
    public Token getToken() throws IllegalCharacterException {
        state = State.READY;

        value.setLength( 0 );

        while ( true ) {

            switch ( state ) {
                case READY: {
                    char current = buffer.getChar();

                    // simple tokens
                    switch ( current ) {
                        case ( '\u0000' ) : {
                            return readyTokens.get( Token.Type.END_OF_PROGRAM );
                        }

                        case ( '+' ) : {
                            return readyTokens.get( Token.Type.PLUS );
                        }

                        case ( '-' ) : {
                            return readyTokens.get( Token.Type.MINUS );
                        }

                        case ( '*' ) : {
                            return readyTokens.get( Token.Type.MULTIPLY );
                        }

                        case ( '/' ) : {
                            char nextChar = buffer.peekChar();

                            if ( nextChar == '/' ) {
                                buffer.getChar();
                                skipOneLineComment();
                                continue;
                            } else if ( nextChar == '*' ) {
                                buffer.getChar();
                                skipMultiLineComment();
                                continue;
                            } else {
                                return readyTokens.get( Token.Type.DIVIDE );
                            }
                        }

                        case ( '(' ) : {
                            return readyTokens.get( Token.Type.OPEN_BRACKET );
                        }

                        case ( ')' ) : {
                            return readyTokens.get( Token.Type.CLOSE_BRACKET );
                        }

                        case ( '{' ) : {
                            return readyTokens.get( Token.Type.OPEN_BRACE );
                        }

                        case ( '}' ) : {
                            return readyTokens.get( Token.Type.CLOSE_BRACE );
                        }

                        case ( ';' ) : {
                            return readyTokens.get( Token.Type.SEMICOLON );
                        }

                        case ( '=' ) : {
                            if ( buffer.peekNextChar() == '=' ) {
                                buffer.getChar();
                                return readyTokens.get( Token.Type.EQUALS );
                            }
                            return readyTokens.get( Token.Type.ASSIGN );
                        }
                    }

                    // complex tokens
                    if ( Character.isLetter( current ) ) {
                        state = State.WORD;
                        value.append( current );
                        continue;
                    } else if ( Character.isDigit( current ) ) {
                        state = State.INTEGER;
                        value.append( current );
                        continue;
                    } else if ( Character.isWhitespace( current ) ) {
                        continue;
                    } else {
                        throw new IllegalCharacterException( current );
                    }
                }

                case INTEGER: {
                    char current = buffer.peekChar();

                    if ( Character.isDigit( current ) ) {
                        buffer.getChar();
                        value.append( current );
                    } else if ( current == '.' ) {
                        buffer.getChar();
                        value.append( current );
                        state = State.DOUBLE;
                    } else {
                        return new Token( Token.Type.INTEGER_VALUE, value.toString() );
                    }
                    break;
                }

                case DOUBLE: {
                    char current = buffer.peekChar();

                    if ( Character.isDigit( current ) ) {
                        buffer.getChar();
                        value.append( current );
                    } else {
                        return new Token( Token.Type.DOUBLE_VALUE, value.toString() );
                    }
                    break;
                }

                case WORD: {
                    char current = buffer.peekChar();

                    if ( Character.isLetter( current ) ) {
                        buffer.getChar();
                        value.append( current );
                    } else {
                        String result = value.toString();

                        if ( keywords.containsKey( result ) ) {
                            return readyTokens.get( keywords.get( result ) );
                        } else {
                            return new Token( Token.Type.IDENTIFIER, value.toString() );
                        }
                    }
                    break;
                }
            }
        }
    }

    private void skipOneLineComment() {
        while ( buffer.getChar() != '\n' ) {
        }
    }

    private void skipMultiLineComment() {
        while ( true ) {
            char current = buffer.getChar();
            if ( ( current == '*' ) && ( buffer.peekChar() == '/' ) ) {
                buffer.getChar();
                return;
            }
        }
    }

}
