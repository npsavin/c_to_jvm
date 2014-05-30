package parser;

import parser.nodes.*;
import parser.nodes.conditional.ConditionalConstructionNode;
import parser.nodes.conditional.ElseBlockNode;
import parser.nodes.conditional.ElseIfBlockNode;
import parser.nodes.conditional.IfBlockNode;
import tokenizer.IllegalCharacterException;
import tokenizer.Token;
import tokenizer.TokenizerInterface;

public class Parser implements ParserInterface {
    private TokenizerInterface tokenizer;

    private Token currentToken;

    public Parser(TokenizerInterface tokenizer) throws ParsingErrorException {
        this.tokenizer = tokenizer;
        getToken();
    }

    public void getToken() throws ParsingErrorException {
        try {
            currentToken = tokenizer.getToken();
        } catch (IllegalCharacterException e) {
            throw new ParsingErrorException("Illegal character: " + e.getMessage());
        }
    }

    public Node parse() throws ParsingErrorException {
        return parseProgram();
    }

    // program > method program | method
    @Override
    public Node parseProgram() throws ParsingErrorException {
        System.out.println("parser.Parser.parseProgram " + currentToken);

        Node list = new Node(NodeType.PROGRAM);

        list.addChild(parseMethod());

        while (!currentToken.hasType(Token.Type.END_OF_PROGRAM)) {
            try {
                list.addChild(parseMethod());
            } catch (ParsingErrorException e) {
                return list;
            }
        }

        return list;
    }

    // method > nodeType name (varList) {body}
    @Override
    public Node parseMethod() throws ParsingErrorException {
        System.out.println("parser.Parser.parseMethod " + currentToken);

        TypeNode type = (TypeNode) parseType();

        Node name = parseName();

        checkAndPassCurrentToken(Token.Type.OPEN_BRACKET);

        Node varList = parseVarList();

        checkAndPassCurrentToken(Token.Type.CLOSE_BRACKET);

        checkAndPassCurrentToken(Token.Type.OPEN_BRACE);

        Node body = parseBody();

        checkAndPassCurrentToken(Token.Type.CLOSE_BRACE);

        return new MethodNode(type, name.getValueToken(), varList, body);
    }

    private void checkAndPassCurrentToken(Token.Type type) throws ParsingErrorException {
        if (!currentToken.hasType(type)) {
            throw new ParsingErrorException(type.toString() + " expected instead of " + currentToken.toString());
        }

        getToken();
    }

    // type > "double" | "int" | "void"
    @Override
    public Node parseType() throws ParsingErrorException {
        System.out.println("parser.Parser.parseType " + currentToken);

        if (!isTypeIdentifier(currentToken)) {
            throw new ParsingErrorException("Type (integer, double or void) expected instead of " + currentToken.toString());
        }

        Node result = new TypeNode(currentToken);
        getToken();

        return result;
    }

    @Override
    public Node parseName() throws ParsingErrorException {
        System.out.println("parser.Parser.parseName " + currentToken);

        if (!currentToken.hasType(Token.Type.IDENTIFIER))
            throw new ParsingErrorException("Identifier expected instead of " + currentToken.toString());

        Node result = new Node(currentToken);
        getToken();

        return result;
    }

    // varList > notEmptyVarList | EMPTY
    @Override
    public Node parseVarList() throws ParsingErrorException {
        System.out.println("parser.Parser.parseVarList " + currentToken);

        Node vars;

        try {
            vars = parseNotEmptyVarList();
        } catch (ParsingErrorException e) {
            //System.out.println( "Empty var list" );
            return new Node(NodeType.LIST);
        }

        return vars;
    }

    // notEmptyVarList > nodeType name | nodeType name, notEmptyVarList
    @Override
    public Node parseNotEmptyVarList() throws ParsingErrorException {
        System.out.println("parser.Parser.parseNotEmptyVarList " + currentToken);
        Node list = new Node(NodeType.LIST);

        TypeNode type = (TypeNode) parseType();
        Node name = parseName();

        VariableNode variable = new VariableNode(type.getValueType(), name.getValueToken().getValue());

        list.addChild(variable);

        while (currentToken.hasType(Token.Type.COMMA)) {
            getToken();

            type = (TypeNode) parseType();
            name = parseName();

            variable = new VariableNode(type.getValueType(), name.getValueToken().getValue());

            list.addChild(variable);
        }

        return list;
    }

    // body > command; | command; body
    @Override
    public BodyNode parseBody() throws ParsingErrorException {
        System.out.println("parser.Parser.parseBody " + currentToken);

        BodyNode body = new BodyNode();

        body.addChild(parseCommand());

        while (currentToken.hasType(Token.Type.SEMICOLON)) {
            getToken();

            try {
                body.addChild(parseCommand());
            } catch (ParsingErrorException e) {
                return body;
            }
        }

        return body;
    }

    // command > name = expr | RETURN expr | nodeType name | name(paramList)
    @Override
    public Node parseCommand() throws ParsingErrorException {
        System.out.println("parser.Parser.parseCommand " + currentToken);

        switch (currentToken.getType()) {
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
                    variableNode.setNodeType(NodeType.ASSIGNED);

                    return variableNode;
                }

                return name;
            }

            case RETURN: {
                Node returnOperator = new Node(currentToken);

                getToken();

                returnOperator.addChild(parseExpression());
                returnOperator.setNodeType(NodeType.RETURN);

                return returnOperator;
            }

            case PRINT: {
                Node printOperator = new Node(currentToken);

                getToken();

                printOperator.addChild(parseExpression());
                printOperator.setNodeType(NodeType.PRINT);

                return printOperator;
            }

            case IF: {
                return parseConditionalConstruction();
            }

            case INTEGER_TYPE:
            case DOUBLE_TYPE:
            case VOID_TYPE:
                TypeNode type = (TypeNode) parseType();

                Node name = parseName();

                VariableNode variable = new VariableNode(type.getValueType(), name.getValueToken().getValue());

                type.addChild(variable);
                type.setNodeType(NodeType.DECLARE);

                return type;
            default:
                throw new ParsingErrorException("Unexpected token while parsing Command: " + currentToken);
        }
    }

    public ConditionalConstructionNode parseConditionalConstruction() throws ParsingErrorException {
        ConditionalConstructionNode conditionalConstructionNode = new ConditionalConstructionNode();

        conditionalConstructionNode.setIfBlockNode(parseIfBlock());

        while (currentToken.hasType(Token.Type.ELSEIF)) {
            conditionalConstructionNode.addElseIfBlock(parseElseIfBlock());
        }

        if (currentToken.hasType(Token.Type.ELSE)) {
            conditionalConstructionNode.setElseBlockNode(parseElseBlock());
        }

        return conditionalConstructionNode;
    }

    public IfBlockNode parseIfBlock() throws ParsingErrorException {
        IfBlockNode ifBlockNode = new IfBlockNode();

        getToken();

        checkAndPassCurrentToken(Token.Type.OPEN_BRACKET);

        ifBlockNode.setCondition(parseExpression());

        checkAndPassCurrentToken(Token.Type.CLOSE_BRACKET);

        checkAndPassCurrentToken(Token.Type.OPEN_BRACE);

        ifBlockNode.setBody(parseBody());

        checkAndPassCurrentToken(Token.Type.CLOSE_BRACE);

        return ifBlockNode;
    }

    public ElseIfBlockNode parseElseIfBlock() throws ParsingErrorException {
        ElseIfBlockNode elseIfBlockNode = new ElseIfBlockNode();

        getToken();

        checkAndPassCurrentToken(Token.Type.OPEN_BRACKET);

        elseIfBlockNode.setCondition(parseExpression());

        checkAndPassCurrentToken(Token.Type.CLOSE_BRACKET);

        checkAndPassCurrentToken(Token.Type.OPEN_BRACE);

        elseIfBlockNode.setBody(parseBody());

        checkAndPassCurrentToken(Token.Type.CLOSE_BRACE);

        return elseIfBlockNode;
    }

    public ElseBlockNode parseElseBlock() throws ParsingErrorException {
        ElseBlockNode elseBlockNode = new ElseBlockNode();

        getToken();

        checkAndPassCurrentToken(Token.Type.OPEN_BRACE);

        elseBlockNode.setBody(parseBody());

        checkAndPassCurrentToken(Token.Type.CLOSE_BRACE);

        return elseBlockNode;
    }


    //paramList > notEmptyParamList | EMPTY
    @Override
    public Node parseParamList() throws ParsingErrorException {
        Node params;

        try {
            params = parseNotEmptyParamList();
        } catch (ParsingErrorException e) {
            return new Node(NodeType.LIST);
        }

        return params;
    }

    //notEmptyParamList > expr | expr, notEmptyParamList
    @Override
    public Node parseNotEmptyParamList() throws ParsingErrorException {
        Node list = new Node(NodeType.LIST);
        list.addChild(parseExpression());

        while (currentToken.hasType(Token.Type.COMMA)) {
            getToken();

            try {
                list.addChild(parseExpression());
            } catch (ParsingErrorException e) {
                return list;
            }
        }

        return list;
    }

    @Override
    public Node parseExpression() throws ParsingErrorException {
        Node root = parseTerm();

        while (isExpressionOperation(currentToken)) {
            Node left = root;

            Token operation = currentToken;

            getToken();

            root = new Node(operation, left, parseTerm());
            root.setNodeType(NodeType.EXPRESSION);
        }

        return root;
    }

    @Override
    public Node parseTerm() throws ParsingErrorException {
        Node root = parseFactor();

        while (isTermOperation(currentToken)) {
            Node left = root;

            Token operation = currentToken;

            getToken();

            root = new Node(operation, left, parseFactor());
            root.setNodeType(NodeType.TERM);
        }

        return root;
    }

    @Override
    public Node parseFactor() throws ParsingErrorException {
        Node first = parsePower();

        if (currentToken.hasType(Token.Type.POWER)) {
            Token operation = currentToken;

            getToken();

            Node power = new Node(operation, first, parseFactor());
            power.setNodeType(NodeType.FACTOR);

            return power;
        }

        return first;
    }

    @Override
    public Node parsePower() throws ParsingErrorException {
        if (currentToken.hasType(Token.Type.MINUS) || currentToken.hasType(Token.Type.NEGATION)) {
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
        switch (currentToken.getType()) {
            case IDENTIFIER:
                Node identifier = new Node(currentToken);

                getToken();

                if (currentToken.hasType(Token.Type.OPEN_BRACKET)) {
                    getToken();

                    Node paramList = parseParamList();

                    MethodCallNode methodCallNode = new MethodCallNode(identifier.getValueToken(), paramList);

                    System.err.println(methodCallNode.toTreeString(0));

                    checkAndPassCurrentToken(Token.Type.CLOSE_BRACKET);

                    return methodCallNode;
                } else {
                    VariableNode variableNode = new VariableNode(identifier.getValueToken().getValue());
                    variableNode.setNodeType(NodeType.VARIABLE_GET);
                    return variableNode;
                }

            case INTEGER_VALUE: {
                ValueNode valueNode = new ValueNode(currentToken);
                valueNode.setValueType(ValueNode.ValueType.INTEGER_VALUE);
                getToken();
                return valueNode;
            }

            case DOUBLE_VALUE: {
                ValueNode valueNode = new ValueNode(currentToken);
                valueNode.setValueType(ValueNode.ValueType.DOUBLE_VALUE);
                getToken();
                return valueNode;
            }

            case MINUS:
                return parseExpression();

            case OPEN_BRACKET:
                getToken();

                Node expression = parseExpression();

                checkAndPassCurrentToken(Token.Type.CLOSE_BRACKET);

                if ( isTokenComparisonOperator(currentToken) ) {
                    Token operation = currentToken;

                    System.out.println(operation);

                    getToken();

                    checkAndPassCurrentToken(Token.Type.OPEN_BRACKET);

                    Node right = parseExpression();

                    checkAndPassCurrentToken(Token.Type.CLOSE_BRACKET);

                    Node result = new Node(operation, expression, right);
                    result.setNodeType(NodeType.EXPRESSION);

                    return result;
                }

                return expression;
        }

        throw new ParsingErrorException("Cannot parse atom: unexpected token " + currentToken);
    }

    private boolean isTokenComparisonOperator(Token token) {
        return token.hasType(Token.Type.EQUALS)
                || token.hasType(Token.Type.GREATER_THAN)
                || token.hasType(Token.Type.GREATER_THAN_OR_EQUALS)
                || token.hasType(Token.Type.LESS_THAN)
                || token.hasType(Token.Type.LESS_THAN_OR_EQUALS);
    }

    private boolean isTermOperation(Token token) {
        return token.hasType(Token.Type.MULTIPLY)
                || token.hasType(Token.Type.DIVIDE)
                || token.hasType(Token.Type.AND);
    }

    private boolean isExpressionOperation(Token token) {
        return token.hasType(Token.Type.PLUS)
                || token.hasType(Token.Type.MINUS)
                || token.hasType(Token.Type.OR);
    }

    private boolean isTypeIdentifier(Token token) {
        return token.hasType(Token.Type.VOID_TYPE)
                || token.hasType(Token.Type.INTEGER_TYPE)
                || token.hasType(Token.Type.DOUBLE_TYPE);
    }
}
