import buffer.Buffer;
import buffer.BufferInterface;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import parser.nodes.Node;
import parser.Parser;
import parser.ParsingErrorException;
import tokenizer.Token;
import tokenizer.Tokenizer;
import tokenizer.TokenizerInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

public class ParserTest {
    Parser parser;

    @Before
    public void setUp() throws Exception {
        BufferInterface buffer = null;
        try {
            buffer = new Buffer( new FileReader( new File( "./resources/testExpression.c" ) ), 10 );
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

        TokenizerInterface tokenzer = new Tokenizer( buffer );
        parser = new Parser( tokenzer );
    }

    private void createParserFromString( String string ) {
        BufferInterface buffer;
        buffer = new Buffer( new StringReader( string ), 10 );

        TokenizerInterface tokenzer = new Tokenizer( buffer );
        try {
            parser = new Parser( tokenzer );
        } catch ( ParsingErrorException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseProgram() throws Exception {

    }

    @Test
    public void testParseMethod() throws Exception {

    }

    @Test
    public void testParseType() throws Exception {

    }

    @Test
    public void testParseName() throws Exception {

    }

    @Test
    public void testParseVarList() throws Exception {

    }

    @Test
    public void testParseNotEmptyVarList() throws Exception {

    }

    @Test
    public void testParseBody() throws Exception {

    }
    @Test
    public void testParseCommand() throws Exception {

    }

    @Test
    public void testParseParamList() throws Exception {

    }

    @Test
    public void testParseNotEmptyParamList() throws Exception {

    }

    @Test
    public void testParseExpression() throws Exception {
        createParserFromString("7+54*2");

        Node expected = new Node (
                new Token(Token.Type.PLUS),
                new Node (new Token( Token.Type.INTEGER_VALUE, "7" ) ),
                new Node (
                        new Token(Token.Type.MULTIPLY),
                        new Node (new Token( Token.Type.INTEGER_VALUE, "54" ) ),
                        new Node (new Token( Token.Type.INTEGER_VALUE, "2" ) )
                )
        );

        Assert.assertEquals( expected, parser.parseExpression() );
    }

    @Test
    public void testParseTerm() throws Exception {
        createParserFromString("7*42.2");

        Node expected = new Node (
                new Token(Token.Type.MULTIPLY),
                new Node (new Token( Token.Type.INTEGER_VALUE, "7" ) ),
                new Node (new Token( Token.Type.DOUBLE_VALUE, "42.2" ) )
        );

        Assert.assertEquals( expected, parser.parseTerm() );
    }

    @Test
    public void testParseFactor() throws Exception {
        createParserFromString("123456");

        Node expected = new Node ( new Token( Token.Type.INTEGER_VALUE, "123456" ) );

        Assert.assertEquals( expected, parser.parseFactor() );

        createParserFromString("-11223");

        expected = new Node (
                new Token(Token.Type.MINUS),
                new Node (new Token( Token.Type.INTEGER_VALUE, "11223" ) ),
                null
        );

        Assert.assertEquals( expected, parser.parseFactor() );
    }

    @Test
    public void testParsePower() throws Exception {
        createParserFromString("1553");

        Node expected = new Node ( new Token( Token.Type.INTEGER_VALUE, "1553" ) );

        Assert.assertEquals( expected, parser.parsePower() );

        createParserFromString("-42745");

        expected = new Node (
                new Token(Token.Type.MINUS),
                new Node (new Token( Token.Type.INTEGER_VALUE, "42745" ) ),
                null
        );

        Assert.assertEquals( expected, parser.parsePower() );
    }

    @Test
    public void testParseAtom() throws Exception {
        createParserFromString("22");

        Node expected = new Node ( new Token( Token.Type.INTEGER_VALUE, "22" ) );

        Assert.assertEquals( expected, parser.parseAtom() );


        createParserFromString("(22+25)");

        expected = new Node(
                new Token(Token.Type.PLUS),
                new Node (new Token( Token.Type.INTEGER_VALUE, "22" ) ),
                new Node (new Token( Token.Type.INTEGER_VALUE, "25" ) )
        );

        Assert.assertEquals( expected, parser.parseAtom() );
    }
}
