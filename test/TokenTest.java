import org.junit.Assert;
import org.junit.Test;

public class TokenTest {
    @Test
    public void testGetType() throws Exception {
        Token equals = new Token( Token.Type.EQUALS );

        Assert.assertEquals( Token.Type.EQUALS, equals.getType() );

        Token identifier = new Token( Token.Type.IDENTIFIER );

        Assert.assertEquals( Token.Type.IDENTIFIER, identifier.getType() );
    }

    @Test
    public void testGetValue() throws Exception {
        Token identifier = new Token( Token.Type.IDENTIFIER, "abc" );

        Assert.assertEquals( "abc", identifier.getValue() );
    }
}
