package parser;

public class VariableNode extends Node {
    private ValueNode.ValueType variableType;
    private String variableName;

    public VariableNode(ValueNode.ValueType variableType, String variableName) {
        this.variableType = variableType;
        this.variableName = variableName;
    }

    public VariableNode(String variableName) {
        this.variableName = variableName;
    }

    public ValueNode.ValueType getVariableType() {
        return variableType;
    }

    public void setVariableType(ValueNode.ValueType variableType) {
        this.variableType = variableType;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String toString() {
        return "VariableNode{" +
                "variableType=" + variableType +
                ", variableName='" + variableName + '\'' +
                '}';
    }

    @Override
    public String toTreeString( int depth ) {
        StringBuilder result = new StringBuilder();

        result.append(variableName);
        if (variableType != null) {
            result.append(" : ").append(variableType);
        }
        result.append('\n');

        if (!children.isEmpty()) {
            for ( int i = 0; i <= depth; i++ ) {
                result.append( "\t" );
            }
            result.append(children.get(0).toTreeString(depth+1));
        }

        return result.toString();
    }
}
