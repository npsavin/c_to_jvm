package parser.nodes.conditional;

import parser.nodes.NodeType;

public class ElseBlockNode extends ConditionalBlockNode {
    public ElseBlockNode() {
        nodeType = NodeType.ELSE_BLOCK;
    }
}
