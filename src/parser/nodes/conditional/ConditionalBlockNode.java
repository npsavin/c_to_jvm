package parser.nodes.conditional;

import parser.nodes.BodyNode;
import parser.nodes.Node;

public abstract class ConditionalBlockNode extends Node {
    protected ConditionalBlockNode() {
    }

    private BodyNode body;
    private Node condition;

    // Getters
    public BodyNode getBody() {
        return body;
    }

    public Node getCondition() {
        return condition;
    }


    // Setters
    public void setBody(BodyNode body) {
        this.body = body;
    }

    public void setCondition(Node condition) {
        this.condition = condition;
    }

    @Override
    public String toTreeString(int depth) {
        StringBuilder result = new StringBuilder();

        result.append(indent(depth))
                .append(nodeType).append('\n');

        if (condition != null) {
            result.append(condition.toTreeString(depth + 1));
        }

        result.append(body.toTreeString(depth + 1));

        return result.toString();
    }
}
