package parser.nodes.conditional;

import parser.nodes.NodeType;

public class IfBlockNode extends ConditionalBlockNode {
    public IfBlockNode() {
        nodeType = NodeType.IF_BLOCK;
    }
}
