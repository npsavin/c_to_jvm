import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

public class TokenizerTest {
    @Test
    public void testGetToken() throws Exception {
        //BufferInterface buffer = new Buffer( new StringReader( "int a = 775; /*comment\n \n * ** 31e1\n 127 == */ \n double basket = 9798.1; return;" ), 10 );

        BufferInterface buffer = null;
        try {
            buffer = new Buffer( new FileReader( new File( "./resources/testProgram.c" ) ), 10 );
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

        TokenizerInterface tokenzer = new Tokenizer( buffer );
        try {
            Assert.assertEquals( Token.Type.VOID_TYPE, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.IDENTIFIER, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.OPEN_BRACKET, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.CLOSE_BRACKET, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.OPEN_BRACE, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.INTEGER_TYPE, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.IDENTIFIER, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.ASSIGN, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.INTEGER_VALUE, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.SEMICOLON, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.DOUBLE_TYPE, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.IDENTIFIER, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.ASSIGN, tokenzer.getToken().getType() );

        } catch ( IllegalCharacterException e ) {
            System.err.println( e.getMessage() );
        }
    }
}
