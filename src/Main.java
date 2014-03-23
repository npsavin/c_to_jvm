import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {
    public static void main( String[] args ) {
        BufferInterface buffer = null;
        try {
            buffer = new Buffer( new FileReader( new File( "./resources/testProgram.c" ) ), 10 );
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

        TokenizerInterface tokenzer = new Tokenizer( buffer );
        try {
            while ( true ) {
                Token token = tokenzer.getToken();

                System.out.println( token.toString() );

                if (token.getType() == Token.Type.END_OF_PROGRAM) {
                    break;
                }
            }
        } catch ( IllegalCharacterException e ) {
            System.err.println( e.getMessage() );
        }
    }
}
