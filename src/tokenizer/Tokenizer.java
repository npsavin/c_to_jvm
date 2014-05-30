package tokenizer;

import buffer.BufferInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tokenizer implements TokenizerInterface {
    private enum State {
        READY,
        INTEGER,
        DOUBLE,
        WORD
    }

    private BufferInterface buffer;

    // create tokens, which can be used as single instances
    private Map<Token.Type, Token> readyTokens = new HashMap<>();

    {
        List<Token.Type> readyTokenTypes = new ArrayList<>();

        // arithmetic operations
        readyTokenTypes.add(Token.Type.PLUS);
        readyTokenTypes.add(Token.Type.MINUS);
        readyTokenTypes.add(Token.Type.MULTIPLY);
        readyTokenTypes.add(Token.Type.DIVIDE);
        readyTokenTypes.add(Token.Type.POWER);

        // assign
        readyTokenTypes.add(Token.Type.ASSIGN);

        // brackets
        readyTokenTypes.add(Token.Type.OPEN_BRACKET);
        readyTokenTypes.add(Token.Type.CLOSE_BRACKET);

        // braces
        readyTokenTypes.add(Token.Type.OPEN_BRACE);
        readyTokenTypes.add(Token.Type.CLOSE_BRACE);

        // comparison
        readyTokenTypes.add(Token.Type.EQUALS);
        readyTokenTypes.add(Token.Type.GREATER_THAN);
        readyTokenTypes.add(Token.Type.GREATER_THAN_OR_EQUALS);
        readyTokenTypes.add(Token.Type.LESS_THAN);
        readyTokenTypes.add(Token.Type.LESS_THAN_OR_EQUALS);

        // logical operators
        readyTokenTypes.add(Token.Type.NEGATION);
        readyTokenTypes.add(Token.Type.AND);
        readyTokenTypes.add(Token.Type.OR);

        // ; and ,
        readyTokenTypes.add(Token.Type.SEMICOLON);
        readyTokenTypes.add(Token.Type.COMMA);

        // type identifiers
        readyTokenTypes.add(Token.Type.INTEGER_TYPE);
        readyTokenTypes.add(Token.Type.DOUBLE_TYPE);
        readyTokenTypes.add(Token.Type.VOID_TYPE);

        // conditional keywords
        readyTokenTypes.add(Token.Type.IF);
        readyTokenTypes.add(Token.Type.ELSE);
        readyTokenTypes.add(Token.Type.ELSEIF);

        // other keywords
        readyTokenTypes.add(Token.Type.RETURN);
        readyTokenTypes.add(Token.Type.PRINT);

        // special token, indicating the end of program
        readyTokenTypes.add(Token.Type.END_OF_PROGRAM);

        // create tokens for all these types
        for ( Token.Type type : readyTokenTypes ) {
            readyTokens.put(type, new Token(type));
        }
    }

    // keywords list
    private Map<String, Token.Type> keywords = new HashMap<>();

    {
        keywords.put("if", Token.Type.IF);
        keywords.put("else", Token.Type.ELSE);
        keywords.put("elseif", Token.Type.ELSEIF);

        keywords.put("return", Token.Type.RETURN);
        keywords.put("print", Token.Type.PRINT);
        keywords.put("int", Token.Type.INTEGER_TYPE);
        keywords.put("double", Token.Type.DOUBLE_TYPE);
        keywords.put("void", Token.Type.VOID_TYPE);
    }

    // map of one-symbol tokens
    private Map<Character, Token.Type> symbols = new HashMap<>();

    {
        symbols.put('\u0000', Token.Type.END_OF_PROGRAM);
        symbols.put('+', Token.Type.PLUS);
        symbols.put('-', Token.Type.MINUS);
        symbols.put('*', Token.Type.MULTIPLY);
        symbols.put('^', Token.Type.POWER);

        symbols.put('!', Token.Type.NEGATION);

        symbols.put('(', Token.Type.OPEN_BRACKET);
        symbols.put(')', Token.Type.CLOSE_BRACKET);

        symbols.put('{', Token.Type.OPEN_BRACE);
        symbols.put('}', Token.Type.CLOSE_BRACE);

        symbols.put(',', Token.Type.COMMA);
        symbols.put(';', Token.Type.SEMICOLON);
    }

    private StringBuilder value = new StringBuilder();

    public Tokenizer(BufferInterface buffer) {
        this.buffer = buffer;
    }

    @Override
    public Token getToken() throws IllegalCharacterException {
        State state = State.READY;

        value.setLength(0);

        while (true) {

            switch (state) {
                case READY: {
                    char current = buffer.getChar();

                    if (symbols.containsKey(current)) {
                        return readyTokens.get(symbols.get(current));
                    }

                    // multi character operator tokens
                    char nextChar = buffer.peekChar();

                    switch (current) {
                        case '/': {
                            if (nextChar == '/') {
                                buffer.getChar();
                                skipOneLineComment();
                                continue;
                            } else if (nextChar == '*') {
                                buffer.getChar();
                                skipMultiLineComment();
                                continue;
                            } else {
                                return readyTokens.get(Token.Type.DIVIDE);
                            }
                        }

                        case '=': {
                            if (nextChar == '=') {
                                buffer.getChar();
                                return readyTokens.get(Token.Type.EQUALS);
                            }
                            return readyTokens.get(Token.Type.ASSIGN);
                        }

                        case '<': {
                            if (nextChar == '=') {
                                buffer.getChar();
                                return readyTokens.get(Token.Type.LESS_THAN_OR_EQUALS);
                            }
                            return readyTokens.get(Token.Type.LESS_THAN);
                        }

                        case '>': {
                            if (nextChar == '=') {
                                buffer.getChar();
                                return readyTokens.get(Token.Type.GREATER_THAN_OR_EQUALS);
                            }
                            return readyTokens.get(Token.Type.GREATER_THAN);
                        }

                        case '&': {
                            if (nextChar == '&') {
                                buffer.getChar();
                                return readyTokens.get(Token.Type.AND);
                            }
                            throw new IllegalCharacterException(current);
                        }

                        case '|': {
                            if (nextChar == '|') {
                                buffer.getChar();
                                return readyTokens.get(Token.Type.OR);
                            }
                            throw new IllegalCharacterException(current);
                        }
                    }

                    // complex tokens
                    if (Character.isLetter(current)) {
                        state = State.WORD;
                        value.append(current);
                        continue;
                    } else if (Character.isDigit(current)) {
                        state = State.INTEGER;
                        value.append(current);
                        continue;
                    } else if (Character.isWhitespace(current)) {
                        continue;
                    } else {
                        throw new IllegalCharacterException(current);
                    }
                }

                case INTEGER: {
                    char current = buffer.peekChar();

                    if (Character.isDigit(current)) {
                        buffer.getChar();
                        value.append(current);
                    } else if (current == '.') {
                        buffer.getChar();
                        value.append(current);
                        state = State.DOUBLE;
                    } else {
                        return new Token(Token.Type.INTEGER_VALUE, value.toString());
                    }
                    break;
                }

                case DOUBLE: {
                    char current = buffer.peekChar();

                    if (Character.isDigit(current)) {
                        buffer.getChar();
                        value.append(current);
                    } else {
                        return new Token(Token.Type.DOUBLE_VALUE, value.toString());
                    }
                    break;
                }

                case WORD: {
                    char current = buffer.peekChar();

                    if (Character.isLetter(current)) {
                        buffer.getChar();
                        value.append(current);
                    } else {
                        String result = value.toString();

                        if (keywords.containsKey(result)) {
                            return readyTokens.get(keywords.get(result));
                        } else {
                            return new Token(Token.Type.IDENTIFIER, value.toString());
                        }
                    }
                    break;
                }
            }
        }
    }

    private void skipOneLineComment() {
        while (true) {
            if (buffer.getChar() == '\n') break;
        }
    }

    private void skipMultiLineComment() {
        while (true) {
            char current = buffer.getChar();
            if ((current == '*') && (buffer.peekChar() == '/')) {
                buffer.getChar();
                return;
            }
        }
    }

}
