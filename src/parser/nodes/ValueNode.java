package parser.nodes;

import tokenizer.Token;

public class ValueNode extends Node {
    public enum ValueType {
        INTEGER_VALUE,
        DOUBLE_VALUE,
        VOID_VALUE
    }

    private ValueType valueType;

    {
        nodeType = NodeType.VALUE;
    }

    public ValueNode(Token token) {
        super(token);
    }

    public ValueType getValueType() {
        return valueType;
    }

    public String getValue() {
        return valueToken.getValue();
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }
}
