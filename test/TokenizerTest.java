import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class TokenizerTest {
    @Test
    public void testGetToken() throws Exception {
        BufferInterface buffer = new Buffer( new StringReader( "a = 775; /*comment\n \n * ** 31e1\n 127 == */ \n basket = 9798;" ), 10 );

        TokenizerInterface tokenzer = new Tokenizer( buffer );
        try {
            Assert.assertEquals( Token.Type.IDENTIFIER, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.ASSIGN, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.INTEGER_TYPE, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.SEMICOLON, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.IDENTIFIER, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.ASSIGN, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.INTEGER_TYPE, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.SEMICOLON, tokenzer.getToken().getType() );
            Assert.assertEquals( Token.Type.END_OF_PROGRAM, tokenzer.getToken().getType() );

        } catch ( IllegalCharacterException e ) {
            System.err.println( e.getMessage() );
        }
    }
}
