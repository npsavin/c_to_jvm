import java.io.StringReader;

public class Main {
    public static void main( String[] args ) {
        BufferInterface buffer = new Buffer( new StringReader( "a = 775; /*comment\n \n * ** 31e1\n 127 == */ \n basket = 9798;" ), 10 );

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
