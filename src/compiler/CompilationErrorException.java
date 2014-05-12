package compiler;

public class CompilationErrorException extends Exception {
    public CompilationErrorException( String message ) {
        super( "Compilation error: " + message );
    }
}
