public class Parser implements ParserInterface {
    private TokenizerInterface tokenizer;

    private Token currentToken;

    public Parser( TokenizerInterface tokenizer ) {
        this.tokenizer = tokenizer;
        try {
            getToken();
        } catch ( IllegalCharacterException e ) {
            e.printStackTrace();
            System.err.println( "No tokens available!" );
        }
    }

    public void getToken() throws IllegalCharacterException {
        //System.out.println("getToken() called");
        currentToken = tokenizer.getToken();
    }

    public Node parse() {
        return parseExpression();
    }

    @Override
    public Node parseExpression() {
        Node root = parseTerm();

        if ( !currentToken.getType().equals( Token.Type.PLUS ) && !currentToken.getType().equals( Token.Type.MINUS ) ) {
            return root;
        }

        while ( currentToken.getType().equals( Token.Type.PLUS ) || currentToken.getType().equals( Token.Type.MINUS )  ) {
            Node left = root;

            Token operation = currentToken;

            try {
                getToken();
            } catch ( IllegalCharacterException e ) {
                e.printStackTrace();
            }
            root = new Node( operation, left, parseTerm() );
        }

        return root;
    }

    @Override
    public Node parseTerm() {
        Node root = parseFactor();

        if ( !currentToken.getType().equals( Token.Type.MULTIPLY ) && !currentToken.getType().equals( Token.Type.DIVIDE ) ) {
            return root;
        }

        while ( currentToken.getType().equals( Token.Type.MULTIPLY ) || currentToken.getType().equals( Token.Type.DIVIDE )  ) {
            Node left = root;

            Token operation = currentToken;

            try {
                getToken();
            } catch ( IllegalCharacterException e ) {
                e.printStackTrace();
            }
            root = new Node( operation, left, parseFactor() );
        }

        return root;
    }

    @Override
    public Node parseFactor() {
        return parsePower();
    }

    @Override
    public Node parsePower() {
        if ( currentToken.getType().equals( Token.Type.MINUS ) ) {
            // make note of unary minus
            try {
                Token operation = currentToken;
                getToken();
                return new Node( operation, parseAtom(), null );
            } catch ( IllegalCharacterException e ) {
                e.printStackTrace();
            }
        }

        return parseAtom();
    }

    @Override
    public Node parseAtom() {
        try {
            Node result;
            switch ( currentToken.getType() ) {
                case IDENTIFIER :
                    result = new Node(currentToken);
                    getToken();
                    return result;
                    //break;
                case INTEGER_VALUE :
                    result = new Node(currentToken);
                    getToken();
                    return result;
                    //break;
                case DOUBLE_VALUE :
                    result = new Node(currentToken);
                    getToken();
                    return result;
                    //break;
                case OPEN_BRACKET :
                    getToken();
                    result = parseExpression();
                    if (currentToken.getType().equals( Token.Type.CLOSE_BRACKET )) {
                        getToken();
                    } else {
                        // incorrect token, should be closing bracket
                        System.err.println( "incorrect token, should be closing bracket" );
                        return null;
                    }
                    return result;
                    //break;
            }

        } catch ( IllegalCharacterException e ) {
            e.printStackTrace();
            return null;
        }

        return null;
    }
}
