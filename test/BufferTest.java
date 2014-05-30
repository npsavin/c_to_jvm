import java.io.Reader;
import java.io.StringReader;

import buffer.Buffer;
import org.junit.*;

public class BufferTest {
    @Test
    public void testGetChar() throws Exception {
        Reader reader = new StringReader( "Y*HE3&Ho87398=" );

        Buffer buffer = new Buffer( reader, 1 );

        Assert.assertEquals( 'Y', buffer.getChar() );
        Assert.assertEquals( '*', buffer.getChar() );

        reader = new StringReader( "Y*HE3&Ho87398=" );

        buffer = new Buffer( reader, 5 );

        Assert.assertEquals( 'Y', buffer.getChar() );
        Assert.assertEquals( '*', buffer.getChar() );
        Assert.assertEquals( 'H', buffer.getChar() );
    }

    @Test
    public void testPeekChar() throws Exception {
        Reader reader = new StringReader( "D=SAoDASO*8asOdaso" );

        Buffer buffer = new Buffer( reader, 10 );

        Assert.assertEquals( 'D', buffer.peekChar() );
        Assert.assertEquals( 'D', buffer.peekChar() );
        Assert.assertEquals( 'D', buffer.peekChar() );

        buffer.getChar();

        Assert.assertEquals( '=', buffer.peekChar() );

        buffer.getChar();
        buffer.getChar();

        Assert.assertEquals( 'A', buffer.peekChar() );
        Assert.assertEquals( 'A', buffer.peekChar() );
    }

    @Test
    public void testPeekNextChar() throws Exception {
        Reader reader = new StringReader( "//ds9a9ds8apd8" );

        Buffer buffer = new Buffer( reader, 10 );

        Assert.assertEquals( '/', buffer.peekNextChar() );
        Assert.assertEquals( '/', buffer.peekNextChar() );

        buffer.getChar();

        Assert.assertEquals( 'd', buffer.peekNextChar() );
        Assert.assertEquals( 'd', buffer.peekNextChar() );

        buffer.getChar();
        buffer.getChar();
        buffer.getChar();

        Assert.assertEquals( 'a', buffer.peekNextChar() );
        Assert.assertEquals( 'a', buffer.peekNextChar() );
    }
}
