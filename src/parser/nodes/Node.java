package parser.nodes;

import tokenizer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Node {

    protected List<Node> children = new ArrayList<>();

    protected NodeType nodeType = NodeType.VALUE;

    protected Token valueToken = null;

    public Node() {
    }

    public Node( NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Node( Token token ) {
        this.valueToken = token;
    }

    public Node( Token token, Node left, Node right ) {
        this.valueToken = token;

        addChild( left );
        addChild( right );
    }

    public void addChild( Node child ) {
        children.add( child );
    }

    public Node getChild( int index ) {
        if ( index >= children.size() ) {
            return null;
        }

        return children.get( index );
    }

    private Node left() {
        return getChild( 0 );
    }

    private Node right() {
        return getChild( 1 );
    }

    public Token getValueToken() {
        return valueToken;
    }

    public void setValueToken(Token valueToken) {
        this.valueToken = valueToken;
    }

    public List<Node> getChildren() {
        return children;
    }

    public String toTreeString( int depth ) {
        StringBuilder result = new StringBuilder();

        if (nodeType != NodeType.VALUE) {
            result.append(indent(depth));
            result.append( nodeType.toString() );
            if ( valueToken != null && ( nodeType == NodeType.EXPRESSION || nodeType == NodeType.TERM ) ) {
                result.append( " " ).append( valueToken.getType().toString() );
            }
            result.append( "\n" );
        } else {
            if ( valueToken != null ) {
                result.append(indent(depth));
                result.append( valueToken.toString() ).append( "\n" );
            } else {
                result.append(indent(depth));
                result.append( "null\n" );
            }
        }

        if (children != null) {
            children.forEach( child -> {
                if ( child != null ) {
                    result.append( child.toTreeString( depth + 1 ) );
                }
            } );
        } else {
            result.append( "<empty>" );
        }

        return result.toString();
    }

    protected String indent(int length) {
        StringBuilder result = new StringBuilder();

        for ( int i = 0; i < length; i++ ) {
            result.append( "| " );
        }

        return result.toString();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Node node = (Node) o;

        if ( children != null ? !children.equals( node.children ) : node.children != null ) return false;
        if ( valueToken != null ? !valueToken.equals( node.valueToken) : node.valueToken != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = children != null ? children.hashCode() : 0;
        result = 31 * result + (valueToken != null ? valueToken.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        Stack<Node> stack = new Stack<>();

        Node current = this;

        while ( current != null || !stack.empty() ) {
            if ( !stack.empty() ) {
                current = stack.pop();

                result.append( current.valueToken).append( "\n" );

                if ( current.right() != null ) {
                    current = current.right();
                } else {
                    current = null;
                }
            }
            while ( current != null ) {
                stack.push( current );
                current = current.left();
            }
        }

        return result.toString();
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
