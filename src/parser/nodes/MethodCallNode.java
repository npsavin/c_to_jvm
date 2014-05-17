package parser.nodes;

import tokenizer.Token;

public final class MethodCallNode extends Node {
    private Node params;

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
        return params;
    }

    public void setParamsList( Node paramsList ) {
        params = paramsList;
    }

    @Override
    public String toTreeString(int depth) {
        StringBuilder result = new StringBuilder();

        result.append(indent(depth))
                .append("METHOD CALL: ").append(getName()).append(" (").append('\n');
        if (!params.getChildren().isEmpty()) {
            params.getChildren().forEach(param ->
                    result.append(param.toTreeString(depth+1)));
        } else {
            result.append(indent(depth+1))
                    .append("<NO PARAMETERS>\n");
        }
        result.append(indent(depth)).append(")\n");

        return result.toString();
    }
}
