package compiler;

import tokenizer.Token;

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

    private static Map<ProgramCompiler.ValueType, String> typeSymbols = new HashMap<>();
    private static Map<StackOperation, String> operationCodes = new HashMap<>();
    private static Map<Token.Type, String> comparisonOperators = new HashMap<>();

    static {
        typeSymbols.put(ProgramCompiler.ValueType.I, "i");
        typeSymbols.put(ProgramCompiler.ValueType.D, "d");
        typeSymbols.put(ProgramCompiler.ValueType.V, "");

        operationCodes.put(StackOperation.ADD, "add");
        operationCodes.put(StackOperation.SUB, "sub");
        operationCodes.put(StackOperation.MUL, "mul");
        operationCodes.put(StackOperation.DIV, "div");

        comparisonOperators.put(Token.Type.EQUALS, "ifeq");
        comparisonOperators.put(Token.Type.GREATER_THAN, "ifgt");
        comparisonOperators.put(Token.Type.GREATER_THAN_OR_EQUALS, "ifge");
        comparisonOperators.put(Token.Type.LESS_THAN, "iflt");
        comparisonOperators.put(Token.Type.LESS_THAN_OR_EQUALS, "ifle");
    }

    public static String methodDeclarationHeader( String signature ) {
        return ".method                  public static " + signature + '\n';
    }

    public static String methodLimits( int limitStack, int limitLocals ) {
        return CODE_INDENT + ".limit stack          " + limitStack + "\n"
                + CODE_INDENT + ".limit locals         " + limitLocals + "\n";
    }

    public static String storeStackToVariable( ProgramCompiler.ValueType type, int index, String name ) throws CompilationErrorException {
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

    public static String storeStackToVariable( ProgramCompiler.Variable variable ) throws CompilationErrorException {
        return storeStackToVariable( variable.getType(), variable.getIndex(), variable.getName() );
    }

    public static String loadVariableToStack( ProgramCompiler.ValueType type, int index, String name ) throws CompilationErrorException {
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

    public static String loadVariableToStack( ProgramCompiler.Variable variable ) throws CompilationErrorException {
        return loadVariableToStack(variable.getType(), variable.getIndex(), variable.getName());
    }

    public static String loadValueToStack( ProgramCompiler.ValueType type, String value) throws CompilationErrorException {
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

    public static String operationOnStack(ProgramCompiler.ValueType type, StackOperation operation) {
        return CODE_INDENT + typeSymbols.get(type) + operationCodes.get(operation) + '\n';
    }

    public static String cast(ProgramCompiler.ValueType from, ProgramCompiler.ValueType to) {
        return CODE_INDENT + typeSymbols.get(from) + "2" + typeSymbols.get(to) + '\n';
    }

    public static String returnOperation(ProgramCompiler.ValueType type) {
        return CODE_INDENT + typeSymbols.get(type) + "return" + '\n';
    }

    public static String getComparisonOperator(Token.Type type) {
        if (comparisonOperators.containsKey(type)) {
            return CODE_INDENT + comparisonOperators.get(type);
        }

        return CODE_INDENT + type.toString();
    }

    public static String getGoto(String label) {
        return CODE_INDENT + "goto " + label;
    }

    public static String startIf(int index) {
        return index + "_start_if";
    }

    public static String endIf(int index) {
        return index + "_end_if";
    }

    public static String startElseIf(int index, int number) {
        return index + "_start_else_if_" + number;
    }

    public static String endElseIf(int index, int number) {
        return index + "_end_else_if_" + number;
    }
}
