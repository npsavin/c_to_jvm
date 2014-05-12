package tokenizer;

public class IllegalCharacterException extends Exception {
    public IllegalCharacterException( char c ) {
        super( "Error at char: \'" + String.valueOf( c ) + "\', code " + (int)c );
    }
}
