package parser.nodes.conditional;

import parser.nodes.NodeType;

public class ElseIfBlockNode extends ConditionalBlockNode{
    public ElseIfBlockNode() {
        nodeType = NodeType.ELSE_IF_BLOCK;
    }
}
