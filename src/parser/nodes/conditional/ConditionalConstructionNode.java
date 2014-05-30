package parser.nodes.conditional;

import parser.nodes.Node;
import parser.nodes.NodeType;

import java.util.ArrayList;
import java.util.List;

public class ConditionalConstructionNode extends Node {
    public ConditionalConstructionNode() {
        nodeType = NodeType.CONDITIONAL_CONSTRUCTION;
    }

    private IfBlockNode ifBlockNode;
    private List<ElseIfBlockNode> elseIfBlockNodes = new ArrayList<>();
    private ElseBlockNode elseBlockNode;

    public boolean hasElseIfBlocks() {
        return !elseIfBlockNodes.isEmpty();
    }

    public boolean hasElseBlock() {
        return elseBlockNode != null;
    }

    // Setters
    public void setIfBlockNode(IfBlockNode ifBlockNode) {
        this.ifBlockNode = ifBlockNode;
    }

    public void addElseIfBlock(ElseIfBlockNode node) {
        elseIfBlockNodes.add(node);
    }

    public void setElseBlockNode(ElseBlockNode elseBlockNode) {
        this.elseBlockNode = elseBlockNode;
    }

    // Getters
    public IfBlockNode getIfBlockNode() {
        return ifBlockNode;
    }

    public List<ElseIfBlockNode> getElseIfBlockNodes() {
        return elseIfBlockNodes;
    }

    public ElseBlockNode getElseBlockNode() {
        return elseBlockNode;
    }

    @Override
    public String toTreeString(int depth) {
        StringBuilder result = new StringBuilder();

        result.append(indent(depth))
                .append("CONDITIONAL CONSTRUCTION\n")
                .append(ifBlockNode.toTreeString(depth + 1));

        for (ElseIfBlockNode elseIfBlockNode : elseIfBlockNodes) {
            result.append(elseIfBlockNode.toTreeString(depth + 1));
        }

        if (elseBlockNode != null) {
            result.append(elseBlockNode.toTreeString(depth + 1));
        }

        return result.toString();
    }
}
