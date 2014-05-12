package parser;

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

        if ( nodeType != NodeType.VALUE) {
            if ( returnType != null ) {
                result.append( returnType.toString() ).append( "\n" );
            }
        } else {
            if ( valueToken != null ) {
                result.append( valueToken.toString() ).append( "\n" );
            } else {
                result.append( "null\n" );
            }
        }

        appendWithIndent( result, "VARLIST\n", depth );

        varList.children.forEach( child -> appendWithIndent( result, child.toTreeString( depth + 2 ), depth + 1 ) );

        if ( children != null ) {
            children.forEach( child -> appendWithIndent( result, child.toTreeString( depth + 1 ), depth ) );
        }


        return result.toString();
    }

    private void appendWithIndent( StringBuilder builder, String value, int indent ) {
        for ( int i = 0; i <= indent; i++ ) {
            builder.append( "\t" );
        }
        builder.append( value );
    }
}
