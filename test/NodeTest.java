import org.junit.Assert;
import org.junit.Test;
import parser.nodes.Node;
import tokenizer.Token;

public class NodeTest {
    @Test
    public void testToString() throws Exception {
        Node node = new Node(
                new Token( Token.Type.PLUS ),
                new Node ( new Token( Token.Type.INTEGER_VALUE, "2" ) ),
                new Node ( new Token( Token.Type.INTEGER_VALUE, "23" ) )
                );

        Assert.assertEquals(
                "tokenizer.Token{nodeType=INTEGER_VALUE, valueToken='2'}\n" +
                "tokenizer.Token{nodeType=PLUS, valueToken='null'}\n" +
                "tokenizer.Token{nodeType=INTEGER_VALUE, valueToken='23'}\n",
                node.toString() );
    }
}
