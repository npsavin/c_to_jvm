package parser;

import tokenizer.Token;

public final class MethodCallNode extends Node {

    {
        nodeType = NodeType.METHOD_CALL;
    }

    public MethodCallNode( Token name, Node params ) {
        super( name );

        setParamsList( params );
    }

    public String getName() {
        return valueToken.getValue();
    }

    public Node getParamsList() {
        return getChild( 0 );
    }

    public void setParamsList( Node paramsList ) {
        children.clear();
        addChild( paramsList );
    }

}
