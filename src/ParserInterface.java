public interface ParserInterface {
    public Node parseExpression();
    public Node parseTerm();
    public Node parseFactor();
    public Node parsePower();
    public Node parseAtom();
}
