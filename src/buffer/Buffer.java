package buffer;

import java.io.IOException;
import java.io.Reader;

public class Buffer implements BufferInterface {
    private Reader reader;

    private char[] charBuffer;
    // 2 is minimum size of buffer
    // necessary for peekNextChar() method
    private int size = 2;
    private int position = 0;

    public Buffer( Reader reader, int size ) {
        this.reader = reader;

        if ( size > 2 ) {
            this.size = size;
        }

        charBuffer = new char[ this.size ];

        try {
            reader.read( charBuffer, 0, this.size );
        } catch ( IOException e ) {
            e.printStackTrace();
            System.err.println("Cannot initialize buffer!");
        }
    }

    private void refresh(  ) {
        charBuffer[0] = charBuffer[size - 1];

        for ( int i = 1; i < size; i++ ) {
            charBuffer[i] = '\u0000';
        }

        try {
            reader.read( charBuffer, 1, size - 1 );
        } catch ( IOException e ) {
            e.printStackTrace();
            System.err.println("Cannot refresh buffer!");
        }

        position = 0;
    }

    @Override
    public char getChar() {
        char result = charBuffer[position];

        position++;

        if ( position >= size - 1 ) {
            refresh();
        }

        return result;
    }

    @Override
    public char peekChar() {
        return charBuffer[position];
    }

    @Override
    public char peekNextChar() {
        return charBuffer[position + 1];
    }
}
