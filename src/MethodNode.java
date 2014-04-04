public class MethodNode extends Node {
    private Token type;
    private Token name;
    private Node varList;
    private Node body;

    public MethodNode( Type type ) {
        super( type );
    }

    public MethodNode( Node parent ) {
        super( parent );
    }

    public MethodNode( Token token ) {
        super( token );
    }

    public MethodNode( Token token, Node left, Node right ) {
        super( token, left, right );
    }

    public MethodNode( Token type, Token name, Node varList, Node body ) {
        super(name);

        setValue( name );
        addChild( body );

        this.type = type;
        this.name = name;
        this.varList = varList;
        this.body = body;

    }

    public Token getResultType() {
        return type;
    }

    public Token getName() {
        return name;
    }

    public Node getVarList() {
        return varList;
    }

    public Node getBody() {
        return body;
    }


}
