import org.junit.Assert;
import org.junit.Test;

public class NodeTest {
    @Test
    public void testToString() throws Exception {
        Node node = new Node(
                new Token( Token.Type.PLUS ),
                new Node ( new Token( Token.Type.INTEGER_VALUE, "2" ) ),
                new Node ( new Token( Token.Type.INTEGER_VALUE, "23" ) )
                );

        Assert.assertEquals(
                "Token{type=INTEGER_VALUE, value='2'}\n" +
                "Token{type=PLUS, value='null'}\n" +
                "Token{type=INTEGER_VALUE, value='23'}\n",
                node.toString() );
    }
}
