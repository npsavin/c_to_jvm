package compiler;

import java.util.HashMap;
import java.util.Map;

public class CodeBuilder {
    public static enum StackOperation {
        ADD,
        SUB,
        MUL,
        DIV
    }

    public static final String CODE_INDENT = "   ";

    public static final String CODE_HEADER =
            ".source                  MainJasmin.java\n" +
                    ".class                   public MainJasmin\n" +
                    ".super                   java/lang/Object\n\n\n";

    public static final String CODE_CONSTRUCTOR =
            ".method                  public <init>()V\n" +
                    CODE_INDENT + ".limit stack          1\n" +
                    CODE_INDENT + ".limit locals         1\n" +
                    CODE_INDENT + ".line                 1\n" +
                    CODE_INDENT + "aload_0\n" +
                    CODE_INDENT + "invokespecial         java/lang/Object/<init>()V\n" +
                    CODE_INDENT + "return\n" +
                    ".end method\n\n";

    public static final String METHOD_MAIN_SIGNATURE = ".method                  public static main([Ljava/lang/String;)V\n";
    public static final String METHOD_END = ".end method\n\n";

    private static Map<Compiler.ValueType, String> typeSymbols = new HashMap<>();
    private static Map<StackOperation, String> operationCodes = new HashMap<>();

    static {
        typeSymbols.put(Compiler.ValueType.I, "i");
        typeSymbols.put(Compiler.ValueType.D, "d");
        typeSymbols.put(Compiler.ValueType.V, "");

        operationCodes.put(StackOperation.ADD, "add");
        operationCodes.put(StackOperation.SUB, "sub");
        operationCodes.put(StackOperation.MUL, "mul");
        operationCodes.put(StackOperation.DIV, "div");
    }

    public static String methodDeclarationHeader( String signature ) {
        return ".method                  public static " + signature + '\n';
    }

    public static String methodLimits( int limitStack, int limitLocals ) {
        return CODE_INDENT + ".limit stack          " + limitStack + "\n"
                + CODE_INDENT + ".limit locals         " + limitLocals + "\n";
    }

    public static String storeStackToVariable( Compiler.ValueType type, int index, String name ) throws CompilationErrorException {
        StringBuilder result = new StringBuilder();

        String typeSymbol = typeSymbols.get(type);

        if ( typeSymbol == null ) {
            throw new CompilationErrorException("Unknown type: " + type);
        }

        result.append(CodeBuilder.CODE_INDENT)
                .append(typeSymbol)
                .append("store                ")
                .append(index)
                .append("    ; stack head -> variable \"")
                .append(name)
                .append("\"")
                .append("\n");

        return result.toString();
    }

    public static String storeStackToVariable( Compiler.Variable variable ) throws CompilationErrorException {
        return storeStackToVariable( variable.getType(), variable.getIndex(), variable.getName() );
    }

    public static String loadVariableToStack( Compiler.ValueType type, int index, String name ) throws CompilationErrorException {
        StringBuilder result = new StringBuilder();

        String typeSymbol = typeSymbols.get(type);

        if ( typeSymbol == null ) {
            throw new CompilationErrorException("values can be assigned only to integer and double type variables");
        }

        result.append(CodeBuilder.CODE_INDENT)
                .append(typeSymbol)
                .append("load                 ")
                .append(index)
                .append("    ; variable \"")
                .append(name)
                .append("\" -> stack head")
                .append("\n");

        return result.toString();
    }

    public static String loadVariableToStack( Compiler.Variable variable ) throws CompilationErrorException {
        return loadVariableToStack(variable.getType(), variable.getIndex(), variable.getName());
    }

    public static String loadValueToStack( Compiler.ValueType type, String value) throws CompilationErrorException {
        StringBuilder result = new StringBuilder();
        switch (type) {
            case I:
                result.append(CodeBuilder.CODE_INDENT)
                        .append("ldc                   ").append(value).append('\n');
                break;

            case D:
                result.append(CodeBuilder.CODE_INDENT)
                        .append("ldc2_w                ").append(value).append('\n');
                break;

            default:
                throw new CompilationErrorException("Unknown type: " + type);
        }

        return result.toString();
    }

    public static String operationOnStack(Compiler.ValueType type, StackOperation operation) {
        return CODE_INDENT + typeSymbols.get(type) + operationCodes.get(operation) + '\n';
    }

    public static String cast(Compiler.ValueType from, Compiler.ValueType to) {
        return CODE_INDENT + typeSymbols.get(from) + "2" + typeSymbols.get(to) + '\n';
    }

    public static String returnOperation(Compiler.ValueType type) {
        return CODE_INDENT + typeSymbols.get(type) + "return" + '\n';
    }
}
