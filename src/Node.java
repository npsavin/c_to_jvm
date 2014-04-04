import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Node {
    public enum Type {
        VALUE,
        OPERATION,
        LIST,
        BODY,
        PROGRAM
    }

    protected List<Node> children = new ArrayList<>();
    private Node parent = null;

    private Type type = Type.VALUE;

    private Token value = null;

    private int childIndex = 0;

    public Node( Type type ) {
        this.type = type;
    }

    public Node( Node parent ) {
        this.parent = parent;
    }

    public Node( Token token ) {
        this.value = token;
    }

    public Node( Token token, Node left, Node right ) {
        this.value = token;

        addChild( left );
        addChild( right );
    }

    public void addChild( Node child ) {
        children.add( child );
    }

    public Node nextChild() {
        if ( childIndex >= children.size() ) {
            return null;
        }

        childIndex++;

        return children.get( childIndex );
    }

    private Node getChild( int index ) {
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

    public Node getParent() {
        return parent;
    }

    public void setParent( Node parent ) {
        this.parent = parent;
    }

    public Token getValue() {
        return value;
    }

    public void setValue( Token value ) {
        this.value = value;
    }

    public String toTreeString( int depth ) {
        StringBuilder result = new StringBuilder();

        if (type != Type.VALUE) {
            result.append( type.toString() ).append( "\n" );
        } else {
            if ( value != null ) {
                result.append( value.toString() ).append( "\n" );
            } else {
                result.append( "null\n" );
            }
        }

        children.forEach( child -> {
            for ( int i = 0; i <= depth; i++ ) {
                result.append( "\t" );
            }
            result.append( child.toTreeString( depth + 1 ) );
        } );

//        if ( left() != null ) {
//            for ( int i = 0; i <= depth; i++ ) {
//                result.append( "\t" );
//            }
//            result.append( "0: " ).append( left().toTreeString( depth + 1 ) );
//        }
//
//        if ( right() != null ) {
//            for ( int i = 0; i <= depth; i++ ) {
//                result.append( "\t" );
//            }
//            result.append( "1: " ).append( right().toTreeString( depth + 1 ) );
//        }

        return result.toString();
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Node node = (Node) o;

        if ( children != null ? !children.equals( node.children ) : node.children != null ) return false;
        if ( value != null ? !value.equals( node.value ) : node.value != null ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = children != null ? children.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
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

                result.append( current.value ).append( "\n" );

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

    public Type getType() {
        return type;
    }

    public void setType( Type type ) {
        this.type = type;
    }
}
