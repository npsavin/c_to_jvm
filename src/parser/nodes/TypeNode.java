package parser.nodes;

import tokenizer.Token;

public class TypeNode extends Node {
    private ValueNode.ValueType valueType;

    public TypeNode( Token token ) {
        super(token);

        if (token.hasType( Token.Type.INTEGER_TYPE )) {
            valueType = ValueNode.ValueType.INTEGER_VALUE;
        } else if (token.hasType( Token.Type.DOUBLE_TYPE )) {
            valueType = ValueNode.ValueType.DOUBLE_VALUE;
        } if (token.hasType( Token.Type.VOID_TYPE )) {
            valueType = ValueNode.ValueType.VOID_VALUE;
        }
    }

    public ValueNode.ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueNode.ValueType valueType) {
        this.valueType = valueType;
    }
}
