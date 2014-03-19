public class Tokenizer implements TokenizerInterface {
    private enum State {
        READY,
        INTEGER,
        WORD,
    }

    private State state;

    private BufferInterface buffer;

    private Token plus = new Token( Token.Type.PLUS );
    private Token minus = new Token( Token.Type.MINUS );
    private Token multiply = new Token( Token.Type.MULTIPLY );
    private Token divide = new Token( Token.Type.DIVIDE );

    private Token openBracket = new Token( Token.Type.OPEN_BRACKET );
    private Token closeBracket = new Token( Token.Type.CLOSE_BRACKET );

    private Token openBrace = new Token( Token.Type.OPEN_BRACE );
    private Token closeBrace = new Token( Token.Type.CLOSE_BRACE );

    private Token assign = new Token( Token.Type.ASSIGN );

    private Token equals = new Token( Token.Type.EQUALS );

    private Token semicolon = new Token( Token.Type.SEMICOLON );

    private Token returnValue = new Token( Token.Type.RETURN );

    private Token endOfProgram = new Token( Token.Type.END_OF_PROGRAM );

    public Tokenizer( BufferInterface buffer ) {
        this.buffer = buffer;
    }

    @Override
    public Token getToken() throws IllegalCharacterException {
        state = State.READY;

        StringBuilder value = null;

        while ( true ) {

            switch ( state ) {
                case READY: {
                    char current = buffer.getChar();

                    // simple tokens
                    switch ( current ) {
                        case ( '\u0000' ) : {
                            return endOfProgram;
                        }

                        case ( '+' ) : {
                            return plus;
                        }

                        case ( '-' ) : {
                            return minus;
                        }

                        case ( '*' ) : {
                            return multiply;
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
                                return divide;
                            }
                        }

                        case ( '(' ) : {
                            return openBracket;
                        }

                        case ( ')' ) : {
                            return closeBracket;
                        }

                        case ( '{' ) : {
                            return openBrace;
                        }

                        case ( '}' ) : {
                            return closeBrace;
                        }

                        case ( ';' ) : {
                            return semicolon;
                        }

                        case ( '=' ) : {
                            if ( buffer.peekNextChar() == '=' ) {
                                buffer.getChar();
                                return equals;
                            }
                            return assign;
                        }
                    }

                    // complex tokens
                    if ( Character.isLetter( current ) ) {
                        state = State.WORD;
                        value = new StringBuilder( ).append( current );
                        continue;
                    } else if ( Character.isDigit( current ) ) {
                        state = State.INTEGER;
                        value = new StringBuilder( ).append( current );
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
                    } else {
                        return new Token( Token.Type.INTEGER, value.toString() );
                    }
                    break;
                }

                case WORD: {
                    char current = buffer.peekChar();

                    if ( Character.isLetter( current ) ) {
                        buffer.getChar();
                        value.append( current );
                    } else {
                        return new Token( Token.Type.IDENTIFIER, value.toString() );
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
