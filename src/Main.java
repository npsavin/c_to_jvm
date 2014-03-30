import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {
    public static void main( String[] args ) {
        BufferInterface buffer = null;
        try {
            buffer = new Buffer( new FileReader( new File( "./resources/testExpression.c" ) ), 10 );
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

        TokenizerInterface tokenzer = new Tokenizer( buffer );
        Parser parser = new Parser( tokenzer );

//        try {
//            parser.getToken();
//        } catch ( IllegalCharacterException e ) {
//            e.printStackTrace();
//        }

        Node result = parser.parse();
        String resultString = result.toTreeString( 0 );
        System.out.println(resultString);
    }
}
