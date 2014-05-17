package parser.nodes;

public class UnaryOperationNode extends Node {
    public enum OperationType {
        UNARY_MINUS
    }

    private Node operand;

    private OperationType operationType;

    public UnaryOperationNode(Node operand, OperationType operationType) {
        this.operand = operand;
        this.operationType = operationType;

        nodeType = NodeType.UNARY_OPERATION;
    }

    public Node getOperand() {
        return operand;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    @Override
    public String toTreeString(int depth) {
        StringBuilder result = new StringBuilder();

        result.append(indent(depth)).append("UNARY_OPERATION(").append(operationType).append(")\n");

        if (operand != null) {
            result.append(operand.toTreeString(depth + 1));
        }

        return result.toString();
    }
}
