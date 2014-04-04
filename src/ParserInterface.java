public interface ParserInterface {
    public Node parseProgram() throws ParsingErrorException;
    public Node parseMethod() throws ParsingErrorException;
    public Node parseType() throws ParsingErrorException;
    public Node parseName() throws ParsingErrorException;
    public Node parseVarList() throws ParsingErrorException;
    public Node parseNotEmptyVarList() throws ParsingErrorException;
    public Node parseBody() throws ParsingErrorException;
    public Node parseCommand() throws ParsingErrorException;
    public Node parseParamList() throws ParsingErrorException;
    public Node parseNotEmptyParamList() throws ParsingErrorException;

    public Node parseExpression() throws ParsingErrorException;
    public Node parseTerm() throws ParsingErrorException;
    public Node parseFactor() throws ParsingErrorException;
    public Node parsePower() throws ParsingErrorException;
    public Node parseAtom() throws ParsingErrorException;
}
