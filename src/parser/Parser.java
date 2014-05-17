package parser;

import parser.nodes.*;
import tokenizer.IllegalCharacterException;
import tokenizer.Token;
import tokenizer.TokenizerInterface;

public class Parser implements ParserInterface {
    private TokenizerInterface tokenizer;

    private Token currentToken;

    public Parser( TokenizerInterface tokenizer ) throws ParsingErrorException {
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
        System.out.println( "parser.Parser.parseProgram " + currentToken );

        Node list = new Node( Node.NodeType.PROGRAM );

        list.addChild( parseMethod() );

        while ( !currentToken.hasType( Token.Type.END_OF_PROGRAM ) ) {
            try {
                list.addChild( parseMethod() );
            } catch ( ParsingErrorException e ) {
                return list;
            }
        }

        return list;
    }

    // method > nodeType name (varList) {body}
    @Override
    public Node parseMethod() throws ParsingErrorException {
        System.out.println( "parser.Parser.parseMethod " + currentToken );

        TypeNode type = (TypeNode) parseType();

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

        return new MethodNode(type, name.getValueToken(), varList, body);
    }

    // type > "double" | "int" | "void"
    @Override
    public Node parseType() throws ParsingErrorException {
        System.out.println( "parser.Parser.parseType " + currentToken );

        if ( !(
                currentToken.hasType( Token.Type.DOUBLE_TYPE ) ||
                currentToken.hasType( Token.Type.INTEGER_TYPE ) ||
                currentToken.hasType( Token.Type.VOID_TYPE )
        ) ) {
            throw new ParsingErrorException( "Type (integer, double or void) expected instead of " + currentToken.toString() );
        }

        Node result = new TypeNode( currentToken );
        getToken();

        return result;
    }

    @Override
    public Node parseName() throws ParsingErrorException {
        System.out.println( "parser.Parser.parseName " + currentToken );

        if ( !currentToken.hasType( Token.Type.IDENTIFIER ) )
            throw new ParsingErrorException( "Identifier expected instead of " + currentToken.toString() );

        Node result = new Node(currentToken);
        getToken();

        return result;
    }

    // varList > notEmptyVarList | EMPTY
    @Override
    public Node parseVarList() throws ParsingErrorException {
        System.out.println( "parser.Parser.parseVarList " + currentToken );

        Node vars;

        try {
            vars = parseNotEmptyVarList();
        } catch ( ParsingErrorException e ) {
            //System.out.println( "Empty var list" );
            return new Node( Node.NodeType.LIST );
        }

        return vars;
    }

    // notEmptyVarList > nodeType name | nodeType name, notEmptyVarList
    @Override
    public Node parseNotEmptyVarList() throws ParsingErrorException {
        System.out.println( "parser.Parser.parseNotEmptyVarList " + currentToken );
        Node list = new Node( Node.NodeType.LIST );

        TypeNode type = (TypeNode) parseType();
        Node name = parseName();

        VariableNode variable = new VariableNode(type.getValueType(), name.getValueToken().getValue());

        list.addChild( variable );

        while (currentToken.hasType( Token.Type.COMMA )) {
            getToken();

            type = (TypeNode) parseType();
            name = parseName();

            variable = new VariableNode(type.getValueType(), name.getValueToken().getValue());

            list.addChild( variable );
        }

        return list;
    }

    // body > command; | command; body
    @Override
    public Node parseBody() throws ParsingErrorException {
        System.out.println( "parser.Parser.parseBody " + currentToken );

        Node list = new Node( Node.NodeType.LIST );
        list.setNodeType(Node.NodeType.BODY);

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

    // command > name = expr | RETURN expr | nodeType name | name(paramList)
    @Override
    public Node parseCommand() throws ParsingErrorException {
        System.out.println( "parser.Parser.parseCommand " + currentToken );

        switch ( currentToken.getType() ) {
            case IDENTIFIER: {
                Node name = parseName();

                if (currentToken.hasType(Token.Type.OPEN_BRACKET)) {
                    getToken();

                    Node params = parseParamList();

                    if (!currentToken.hasType(Token.Type.CLOSE_BRACKET)) throw new ParsingErrorException();

                    MethodCallNode methodCallNode = new MethodCallNode(
                            name.getValueToken(),
                            params
                    );

                    getToken();

                    return methodCallNode;
                } else if (currentToken.hasType(Token.Type.ASSIGN)) {
                    VariableNode variableNode = new VariableNode(name.getValueToken().getValue());

                    Node assignOperator = new Node(currentToken);

                    getToken();

                    assignOperator.addChild(parseExpression());

                    variableNode.addChild(assignOperator);
                    variableNode.setNodeType(Node.NodeType.ASSIGNED);

                    return variableNode;
                }

                return name;
            }

            case RETURN: {
                Node returnOperator = new Node(currentToken);

                getToken();

                returnOperator.addChild(parseExpression());
                returnOperator.setNodeType(Node.NodeType.RETURN);

                return returnOperator;
            }

            case PRINT: {
                Node printOperator = new Node(currentToken);

                getToken();

                printOperator.addChild(parseExpression());
                printOperator.setNodeType(Node.NodeType.PRINT);

                return printOperator;
            }

            case INTEGER_TYPE:
            case DOUBLE_TYPE:
            case VOID_TYPE:
                TypeNode type = (TypeNode) parseType();

                Node name = parseName();

                VariableNode variable = new VariableNode(type.getValueType(), name.getValueToken().getValue());

                type.addChild( variable );
                type.setNodeType(Node.NodeType.DECLARE);

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
            return new Node( Node.NodeType.LIST );
        }

        return params;
    }

    //notEmptyParamList > expr | expr, notEmptyParamList
    @Override
    public Node parseNotEmptyParamList() throws ParsingErrorException {
        Node list = new Node( Node.NodeType.LIST );
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
            root.setNodeType(Node.NodeType.EXPRESSION);
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
            root.setNodeType(Node.NodeType.TERM);
        }

        return root;
    }

    @Override
    public Node parseFactor() throws ParsingErrorException {
        Node first = parsePower();

        if ( currentToken.hasType( Token.Type.POWER ) ) {
            Token operation = currentToken;

            getToken();

            Node power = new Node( operation, first, parseFactor() );
            power.setNodeType(Node.NodeType.FACTOR);

            return power;
        }

        return first;
    }

    @Override
    public Node parsePower() throws ParsingErrorException {
        if ( currentToken.hasType( Token.Type.MINUS ) ) {
            // pass minus
            getToken();

            return new UnaryOperationNode(
                    parseAtom(),
                    UnaryOperationNode.OperationType.UNARY_MINUS);
        }

        return parseAtom();
    }

    @Override
    public Node parseAtom() throws ParsingErrorException {
        switch ( currentToken.getType() ) {
            case IDENTIFIER :
                Node identifier = new Node(currentToken);

                getToken();

                if (currentToken.hasType( Token.Type.OPEN_BRACKET )) {
                    getToken();

                    Node paramList = parseParamList();

                    MethodCallNode methodCallNode = new MethodCallNode( identifier.getValueToken(), paramList );

                    System.err.println(methodCallNode.toTreeString(0));

                    if ( currentToken.hasType( Token.Type.CLOSE_BRACKET ) ) {
                        getToken();
                    } else {
                        throw new ParsingErrorException(
                                "Expected close bracket ')' at the end of arguments list, but found " + currentToken );
                    }

                    return methodCallNode;
                } else {
                    VariableNode variableNode = new VariableNode(identifier.getValueToken().getValue());
                    variableNode.setNodeType(Node.NodeType.VARIABLE_GET);
                    return variableNode;
                }

            case INTEGER_VALUE : {
                ValueNode valueNode = new ValueNode(currentToken);
                valueNode.setValueType(ValueNode.ValueType.INTEGER_VALUE);
                getToken();
                return valueNode;
            }

            case DOUBLE_VALUE : {
                ValueNode valueNode = new ValueNode(currentToken);
                valueNode.setValueType(ValueNode.ValueType.DOUBLE_VALUE);
                getToken();
                return valueNode;
            }

            case MINUS:
                return parseExpression();

            case OPEN_BRACKET :
                getToken();
                Node expression = parseExpression();
                if (currentToken.hasType( Token.Type.CLOSE_BRACKET )) {
                    getToken();
                } else {
                    // incorrect token, should be closing bracket
                    throw new ParsingErrorException(
                            "Close bracket ')' expected instead of " + currentToken );
                }

                return expression;
        }

        throw new ParsingErrorException("Cannot parse atom: unexpected token " + currentToken);
    }
}
