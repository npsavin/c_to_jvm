public class Parser implements ParserInterface {
    private TokenizerInterface tokenizer;

    private Token currentToken;

    public Parser( TokenizerInterface tokenizer ) throws ParsingErrorException{
        this.tokenizer = tokenizer;
        getToken();
    }

    public void getToken() throws ParsingErrorException {
        try {
            currentToken = tokenizer.getToken();
        } catch ( IllegalCharacterException e ) {
            throw new ParsingErrorException("Illegal character!");
        }
    }

    public Node parse() throws ParsingErrorException {
        return parseProgram();
    }

    // program > method program | method
    @Override
    public Node parseProgram() throws ParsingErrorException {
        System.out.println( "Parser.parseProgram " + currentToken );

        Node list = new Node( Node.Type.PROGRAM );

        list.addChild( parseMethod() );

        while ( !currentToken.hasType( Token.Type.END_OF_PROGRAM )) {
            try {
                list.addChild( parseMethod() );
            } catch ( ParsingErrorException e ) {
                return list;
            }
        }

        return list;
    }

    // method > type name (varList) {body}
    @Override
    public Node parseMethod() throws ParsingErrorException {
        System.out.println( "Parser.parseMethod " + currentToken );

        Node type = parseType();

        Node name = parseName();

        if ( !currentToken.hasType( Token.Type.OPEN_BRACKET ) )
            throw new ParsingErrorException( "Open bracket '(' expected instead of " + currentToken.toString() );

        getToken();

        Node varList = parseVarList();

        if ( !currentToken.hasType( Token.Type.CLOSE_BRACKET ) )
            throw new ParsingErrorException( "Close bracket ')' expected instead of " + currentToken.toString() );

        getToken();

        if ( !currentToken.hasType( Token.Type.OPEN_BRACE ) )
            throw new ParsingErrorException( "Open brace '{' expected instead of " + currentToken.toString() );

        getToken();

        Node body = parseBody();

        if ( !currentToken.hasType( Token.Type.CLOSE_BRACE ) )
            throw new ParsingErrorException( "Close brace '}' expected instead of " + currentToken.toString() );

        getToken();

        return new MethodNode(type.getValue(), name.getValue(), varList, body);
    }

    // type > "double" | "int" | "void"
    @Override
    public Node parseType() throws ParsingErrorException {
        System.out.println( "Parser.parseType " + currentToken );

        if ( !(
                currentToken.hasType( Token.Type.DOUBLE_TYPE ) ||
                currentToken.hasType( Token.Type.INTEGER_TYPE ) ||
                currentToken.hasType( Token.Type.VOID_TYPE )
        ) )
            throw new ParsingErrorException( "Type (integer, double or void) expected instead of " + currentToken.toString() );

        Node result = new Node( currentToken );
        getToken();

        return result;
    }

    @Override
    public Node parseName() throws ParsingErrorException {
        System.out.println( "Parser.parseName " + currentToken );

        if ( !currentToken.hasType( Token.Type.IDENTIFIER ) )
            throw new ParsingErrorException( "Identifier expected instead of " + currentToken.toString() );

        Node result = new Node(currentToken);
        getToken();

        return result;
    }

    // varList > notEmptyVarList | EMPTY
    @Override
    public Node parseVarList() throws ParsingErrorException {
        System.out.println( "Parser.parseVarList " + currentToken );

        Node vars;

        try {
            vars = parseNotEmptyParamList();
        } catch ( ParsingErrorException e ) {
            return new Node( Node.Type.LIST );
        }

        return vars;
    }

    // notEmptyVarList > type name | type name, notEmptyVarList
    @Override
    public Node parseNotEmptyVarList() throws ParsingErrorException {
        Node list = new Node( Node.Type.LIST );

        Node type = parseType();
        Node name = parseName();

        type.addChild( name );

        list.addChild( type );

        while (currentToken.hasType( Token.Type.COMMA )) {
            getToken();

            type = parseType();
            name = parseName();

            type.addChild( name );

            list.addChild( type );
        }

        return list;
    }

    // body > command; | command; body
    @Override
    public Node parseBody() throws ParsingErrorException {
        System.out.println( "Parser.parseBody " + currentToken );

        Node list = new Node( Node.Type.LIST );
        list.setType( Node.Type.BODY );

        list.addChild( parseCommand() );

        while (currentToken.hasType( Token.Type.SEMICOLON )) {
            getToken();

            try {
                list.addChild( parseCommand() );
            } catch ( ParsingErrorException e ) {
                return list;
            }
        }

        return list;
    }

    // command > name = expr | RETURN expr | type name | name(paramList)
    @Override
    public Node parseCommand() throws ParsingErrorException {
        System.out.println( "Parser.parseCommand " + currentToken );

        switch ( currentToken.getType() ) {
            case IDENTIFIER:
                Node name = parseName();

                if (currentToken.hasType( Token.Type.OPEN_BRACKET )) {
                    Node params = parseParamList();

                    if ( !currentToken.hasType( Token.Type.CLOSE_BRACKET ) ) throw new ParsingErrorException();

                    getToken();

                    name.addChild( params );
                } else if ( currentToken.hasType( Token.Type.ASSIGN ) ) {
                    Node assignOperator = new Node( currentToken );

                    getToken();

                    assignOperator.addChild( parseExpression() );

                    name.addChild( assignOperator );
                }

                return name;

            case RETURN:
                Node returnOperator = new Node( currentToken );

                getToken();

                returnOperator.addChild( parseExpression() );

                return returnOperator;

            case INTEGER_TYPE:
            case DOUBLE_TYPE:
            case VOID_TYPE:
                Node type = new Node( currentToken );

                getToken();

                type.addChild( parseName() );

                return type;
            default:
                throw new ParsingErrorException("Unexpected token while parsing Command: " + currentToken);
        }
    }

    //paramList > notEmptyParamList | EMPTY
    @Override
    public Node parseParamList() throws ParsingErrorException {
        Node params;

        try {
            params = parseNotEmptyParamList();
        } catch ( ParsingErrorException e ) {
            return new Node( Node.Type.LIST );
        }

        return params;
    }

    //notEmptyParamList > expr | expr, notEmptyParamList
    @Override
    public Node parseNotEmptyParamList() throws ParsingErrorException {
        Node list = new Node( Node.Type.LIST );
        list.addChild( parseExpression() );

        while (currentToken.hasType( Token.Type.COMMA )) {
            getToken();

            try {
                list.addChild( parseExpression() );
            } catch ( ParsingErrorException e ) {
                return list;
            }
        }

        return list;
    }

    @Override
    public Node parseExpression() throws ParsingErrorException {
        Node root = parseTerm();

        if ( !currentToken.hasType( Token.Type.PLUS ) && !currentToken.hasType( Token.Type.MINUS ) ) {
            return root;
        }

        while ( currentToken.hasType( Token.Type.PLUS ) || currentToken.hasType( Token.Type.MINUS ) ) {
            Node left = root;

            Token operation = currentToken;

            getToken();

            root = new Node( operation, left, parseTerm() );
        }

        return root;
    }

    @Override
    public Node parseTerm() throws ParsingErrorException {
        Node root = parseFactor();

        if ( !currentToken.hasType( Token.Type.MULTIPLY ) && !currentToken.hasType( Token.Type.DIVIDE ) ) {
            return root;
        }

        while ( currentToken.hasType( Token.Type.MULTIPLY ) || currentToken.hasType( Token.Type.DIVIDE )  ) {
            Node left = root;

            Token operation = currentToken;

            getToken();

            root = new Node( operation, left, parseFactor() );
        }

        return root;
    }

    @Override
    public Node parseFactor() throws ParsingErrorException {
        Node first = parsePower();

        if ( currentToken.hasType( Token.Type.POWER ) ) {
            Token operation = currentToken;

            getToken();

            return new Node( operation, first, parseFactor() );
        }

        return first;
    }

    @Override
    public Node parsePower() throws ParsingErrorException {
        if ( currentToken.hasType( Token.Type.MINUS ) ) {
            Token operation = currentToken;

            getToken();

            return new Node( operation, parseAtom(), null );
        }

        return parseAtom();
    }

    @Override
    public Node parseAtom() throws ParsingErrorException {
        Node result;
        switch ( currentToken.getType() ) {
            case IDENTIFIER :
                result = new Node(currentToken);
                result.setValue( currentToken );
                getToken();
                return result;

            case INTEGER_VALUE :
            case DOUBLE_VALUE :
                result = new Node( currentToken );
                result.setValue( currentToken );
                getToken();
                return result;

            case OPEN_BRACKET :
                getToken();
                result = parseExpression();
                if (currentToken.hasType( Token.Type.CLOSE_BRACKET )) {
                    getToken();
                } else {
                    // incorrect token, should be closing bracket
                    throw new ParsingErrorException( "Close bracket expected instead of " + currentToken );
                }
                return result;
        }

        return null;
    }
}
