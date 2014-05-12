package parser;

public class AssignNode extends Node {
    private VariableNode to;
    private Node from;

    {
        nodeType = NodeType.ASSIGNED;
    }

    public AssignNode(VariableNode to, Node from) {
        this.to = to;
        this.from = from;
    }

    public VariableNode getTo() {
        return to;
    }

    public Node getFrom() {
        return from;
    }

    @Override
    public String toTreeString(int depth) {
        return to.getVariableName() + " = \n" + from.toTreeString(depth+1);
    }
}
