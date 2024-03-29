import buffer.Buffer;
import buffer.BufferInterface;
import compiler.ProgramCompiler;
import parser.Parser;
import parser.ParsingErrorException;
import parser.nodes.Node;
import tokenizer.Tokenizer;
import tokenizer.TokenizerInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Main {
    public static void main( String[] args ) {
        BufferInterface buffer = null;
        try {
            buffer = new Buffer( new FileReader( new File( "./resources/testIfProgram.c" ) ), 10 );
        } catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }

        TokenizerInterface tokenizer = new Tokenizer( buffer );
        Parser parser;

        try {
            parser = new Parser( tokenizer );

            Node result = parser.parse();
            String resultString = result.toTreeString( 0 );
            System.out.println(resultString);

            ProgramCompiler compiler = new ProgramCompiler();
            compiler.compileProgram( result );
        } catch ( ParsingErrorException e ) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
        }

    }
}
