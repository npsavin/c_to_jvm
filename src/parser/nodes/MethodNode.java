package parser.nodes;

import tokenizer.Token;

public class MethodNode extends Node {
    private TypeNode returnType;
    private Token name;
    private Node varList;
    private Node body;

    public MethodNode( TypeNode returnType, Token name, Node varList, Node body ) {
        super(name);

        setValueToken(name);
        addChild( body );

        this.returnType = returnType;
        this.name = name;
        this.varList = varList;
        this.body = body;
    }

    public ValueNode.ValueType getResultType() {
        return returnType.getValueType();
    }

    public Token getName() {
        return name;
    }

    public Node getVarList() {
        return varList;
    }

    public Node getBody() {
        return body;
    }

    public String toTreeString( int depth ) {
        StringBuilder result = new StringBuilder();

        result.append(indent(depth))
                .append("METHOD ").append(name.getValue())
                .append(" RETURN ").append(returnType.getValueType())
                .append('\n');

        result.append(indent(depth+1))
                .append("ARGLIST:").append('\n');

        if (varList.children != null) {
            varList.children.forEach(child -> result.append(indent(depth+2)).append(child.toString()).append('\n'));
        }

        result.append(indent(depth+1))
                .append("BODY:").append('\n');

        if ( children != null ) {
            children.forEach( child -> result.append(child.toTreeString(depth+2)).append('\n') );
        }

        return result.toString();
    }

}
