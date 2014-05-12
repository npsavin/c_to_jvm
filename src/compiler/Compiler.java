package compiler;

import parser.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Compiler {
    private static final String OUT_FILE_PATH = "./jasminOut/MainJasmin.j";

    private static final String TAB_CODE = "   ";

    private static final String HEADER_CODE =
            ".source                  MainJasmin.java\n" +
                    ".class                   public MainJasmin\n" +
                    ".super                   java/lang/Object\n\n\n";

    private static final String CONSTRUCTOR_CODE =
            ".method                  public <init>()V\n" +
                    TAB_CODE + ".limit stack          1\n" +
                    TAB_CODE + ".limit locals         1\n" +
                    TAB_CODE + ".line                 1\n" +
                    TAB_CODE + "aload_0\n" +
                    TAB_CODE + "invokespecial         java/lang/Object/<init>()V\n" +
                    TAB_CODE + "return\n" +
                    ".end method\n\n";


    private Map<ValueNode.ValueType, ValueType> typeMap = new HashMap<>();
    private Map<String, Variable> variableMap = new HashMap<>();
    private Map<String, MethodNode> methodMap = new HashMap<>();

    private int[] localVariables = new int[100];
    private int stackDepth = 0;
    private int maxStackDepth = 0;

    private MethodNode currentMethod;

    private ValueType typeOnStack;

    public Compiler() {
        typeMap.put(ValueNode.ValueType.INTEGER_VALUE, ValueType.I);
        typeMap.put(ValueNode.ValueType.DOUBLE_VALUE, ValueType.D);
        typeMap.put(ValueNode.ValueType.VOID_VALUE, ValueType.V);
    }

    public void compileProgram(Node root) {
        FileWriter writer;
        try {
            createFile();
            writer = new FileWriter(new File(OUT_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Cannot open file: " + OUT_FILE_PATH);

            return;
        }

        StringBuilder builder = new StringBuilder();

        try {
            builder.append(HEADER_CODE);
            builder.append(CONSTRUCTOR_CODE);

            for (Node method : root.getChildren()) {
                builder.append(compileMethod(method));
            }

            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException | CompilationErrorException e) {
            e.printStackTrace();
        }
    }

    private String compileMethod(Node node) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        variableMap = new HashMap<>();
        stackDepth = 0;
        maxStackDepth = 0;

        builder.append(".method                  ");

        MethodNode methodNode = (MethodNode) node;

        methodMap.put(methodNode.getName().getValue(), methodNode);

        currentMethod = methodNode;

        // generate method signature
        if (methodNode.getName().getValue().equals("main")) {
            builder.append("public static main([Ljava/lang/String;)V\n");
        } else {
            builder.append("public static ")
                    .append(constructMetodSignature(methodNode))
                    .append('\n');
        }

        String signature = builder.toString();
        builder.setLength(0);

        List<Node> variables = methodNode.getVarList().getChildren();

        // declare all variables
        for (Node var : variables) {
            VariableNode variable = (VariableNode) var;
            declareVariable(variable.getVariableName(), typeMap.get(variable.getVariableType()));
        }

        // move arguments from stack to variables
        for (int i = variables.size() - 1; i >= 0; i--) {
            VariableNode var = (VariableNode) variables.get(i);

            builder.append(storeStackToVariable(var.getVariableName()));
        }

        // compile method commands
        for (Node command : methodNode.getBody().getChildren()) {
            builder.append(compileCommand(command));
        }

        String body = builder.toString();

        builder.setLength(0);

        // reserve 2 words for temporary variable
        int variablesAmount = 2;
        for (Variable var : variableMap.values()) {
            switch (var.type) {
                case I:
                    variablesAmount += 1;
                    break;

                case D:
                    variablesAmount += 2;
                    break;
            }
        }

        builder.append(TAB_CODE)
                .append(".limit stack          ").append(maxStackDepth).append("\n")
                .append(TAB_CODE)
                .append(".limit locals         ").append(variablesAmount).append("\n");

        String limits = builder.toString();

        builder.setLength(0);

        String footer = ".end method\n\n";
        currentMethod = null;

        builder.append(signature).append(limits).append(body).append(footer);

        return builder.toString();
    }

    private String compileCommand(Node command) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();
        switch (command.getNodeType()) {
            case ASSIGNED:
                builder.append(compileAssignValue(command));
                break;

            case RETURN:
                builder.append(compileReturn(command));
                break;

            case PRINT:
                builder.append(compilePrint(command));
                break;

            case DECLARE:
                builder.append(compileVariableDeclaration(command));
                break;

            case METHOD_CALL:
                builder.append(compileMethodCall((MethodCallNode) command));
                break;

            default:
                builder.append(TAB_CODE)
                        .append("UNEXPECTED NODE\n");
                break;
        }

        return builder.toString();
    }

    private String compileReturn(Node command) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        Node expression = command.getChild(0);

        if (expression != null) {
            builder.append(compileExpression(command.getChild(0)));
        }

        String suffix;

        switch (currentMethod.getResultType()) {
            case INTEGER_VALUE:
                suffix = "i";
                break;

            case DOUBLE_VALUE:
                suffix = "d";
                break;

            case VOID_VALUE:
                suffix = "";
                break;

            default:
                throw new CompilationErrorException("method returns value of unknown type");
        }

        builder.append(TAB_CODE)
                .append(suffix)
                .append("return\n");

        return builder.toString();
    }

    private String compilePrint(Node command) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        builder.append(TAB_CODE)
                .append("getstatic             java/lang/System/out Ljava/io/PrintStream;\n");

        Node expression = command.getChild(0);

        if (expression != null) {
            builder.append(compileExpression(expression));
        }

        String type = typeOnStack.toString();

        builder.append(TAB_CODE).append("invokevirtual         java/io/PrintStream/println(").append(type).append(")V\n");

        return builder.toString();
    }

    private String compileAssignValue(Node command) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        Node expression = command.getChild(0).getChild(0);

        builder.append(compileExpression(expression));

        builder.append(storeStackToVariable(((VariableNode) command).getVariableName()));

        return builder.toString();
    }

    private String compileVariableDeclaration(Node command) {
        declareVariable(
                ((VariableNode) command.getChild(0)).getVariableName(),
                typeMap.get(((TypeNode) command).getValueType()));

        return "";
    }

    private String compileExpression(Node expression) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        if (expression.getNodeType() != Node.NodeType.EXPRESSION) {
            builder.append(compileTerm(expression));
        } else {
            builder.append(compileTerm(expression.getChild(0)));
            ValueType firstType = typeOnStack;

            String operation;

            switch (expression.getValueToken().getType()) {
                case PLUS:
                    operation = "add";
                    break;
                case MINUS:
                    operation = "sub";
                    break;
                default:
                    throw new CompilationErrorException("Unknown operation with two parameters");
            }

            builder.append(compileTerm(expression.getChild(1)));
            ValueType secondType = typeOnStack;

            if (firstType == ValueType.I && secondType == ValueType.I) {
                builder.append(TAB_CODE)
                        .append("i").append(operation).append("\n");
                typeOnStack = ValueType.I;
            } else {
                if (secondType == ValueType.I) {
                    // cast second argument ( stack top ) to double
                    builder.append(TAB_CODE).append("i2d                   ; cast integer on stack to double, for ")
                            .append(operation)
                            .append('\n');
                } else if (firstType == ValueType.I) {
                    // cast first argument ( stack second ) to double
                    builder.append(TAB_CODE)
                            .append("dstore                ").append(getMinVariableIndex(ValueType.D))
                            .append("    ; temporary")
                            .append("\n");
                    builder.append(TAB_CODE).append("i2d                   ; cast integer on stack to double, for ")
                            .append(operation)
                            .append('\n');
                    builder.append(TAB_CODE).append("dload                 ").append(getMinVariableIndex(ValueType.D))
                            .append("    ; temporary")
                            .append("\n");
                }

                builder.append(TAB_CODE)
                        .append("d").append(operation).append("\n");
                typeOnStack = ValueType.D;
                stackDepth--;
            }
        }

        return builder.toString();
    }

    private String compileTerm(Node term) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        if (term.getNodeType() != Node.NodeType.TERM) {
            builder.append(compileFactor(term));
        } else {
            builder.append(compileFactor(term.getChild(0)));
            ValueType firstType = typeOnStack;

            String operation;

            switch (term.getValueToken().getType()) {
                case MULTIPLY:
                    operation = "mul";
                    break;
                case DIVIDE:
                    operation = "div";
                    break;
                default:
                    throw new CompilationErrorException("Unknown operation with two parameters: " + term.getValueToken().getType());
            }

            builder.append(compileTerm(term.getChild(1)));
            ValueType secondType = typeOnStack;

            if (firstType == ValueType.I && secondType == ValueType.I) {
                builder.append(TAB_CODE)
                        .append("i").append(operation).append("\n");
                typeOnStack = ValueType.I;
            } else {
                if (secondType == ValueType.I) {
                    // cast second argument ( stack top ) to double
                    builder.append(TAB_CODE)
                            .append("i2d                   ; cast integer on stack to double, for ")
                            .append(operation)
                            .append('\n');
                } else if (firstType == ValueType.I) {
                    // cast first argument ( stack second ) to double
                    builder.append(TAB_CODE).append("dstore                ").append(getMinVariableIndex(ValueType.D))
                            .append("    ; temporary")
                            .append("\n");
                    builder.append(TAB_CODE).append("i2d                   ; cast integer on stack to double, for ")
                            .append(operation)
                            .append('\n');
                    builder.append(TAB_CODE).append("dload                 ").append(getMinVariableIndex(ValueType.D))
                            .append("    ; temporary")
                            .append("\n");
                }

                builder.append(TAB_CODE)
                        .append("d").append(operation).append("\n");
                typeOnStack = ValueType.D;
                stackDepth--;
            }
        }

        return builder.toString();
    }

    private String compileFactor(Node factor) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        if (factor.getNodeType() != Node.NodeType.FACTOR) {
            builder.append(compilePower(factor));
        } else {
            builder.append(compilePower(factor.getChild(0)));
            ValueType firstType = typeOnStack;

            String operation;

            switch (factor.getValueToken().getType()) {
                case POWER:
                    operation = "POWER";
                    break;
                default:
                    throw new CompilationErrorException("Unknown operation with two parameters: " + factor.getValueToken().getType());
            }

            builder.append(compileFactor(factor.getChild(1)));
            ValueType secondType = typeOnStack;

            if (firstType == ValueType.I && secondType == ValueType.I) {
                builder.append(TAB_CODE)
                        .append("i").append(operation).append("\n");
                typeOnStack = ValueType.I;
            } else {
                if (firstType == ValueType.D) {
                    // cast second argument ( stack top ) to double
                    builder.append(TAB_CODE).append("i2d                   ; cast integer on stack to double\n");
                } else if (secondType == ValueType.D) {
                    // cast first argument ( stack second ) to double
                    builder.append(TAB_CODE).append("dstore                ").append(getMinVariableIndex(ValueType.D)).append("\n");
                    builder.append(TAB_CODE).append("i2d                   ; cast integer on stack to double\n");
                    builder.append(TAB_CODE).append("dload                 ").append(getMinVariableIndex(ValueType.D)).append("\n");
                }

                builder.append(TAB_CODE)
                        .append("d").append(operation).append("\n");
                typeOnStack = ValueType.D;

                stackDepth--;
            }
        }

        return builder.toString();
    }

    // TODO: unary minus does not work
    private String compilePower(Node power) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        if (power.getNodeType() == Node.NodeType.TERM) {
            builder.append(compileAtom(power));
            builder.append(TAB_CODE).append("APPLY UNARY MINUS to ").append(power.getNodeType()).append('\n');
        } else {
            builder.append(compileAtom(power));
        }

        return builder.toString();
    }

    private String compileAtom(Node atom) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        switch (atom.getNodeType()) {
            case VALUE:
                ValueNode valueNode = (ValueNode) atom;

                switch (valueNode.getValueType()) {
                    case INTEGER_VALUE:
                        builder.append(TAB_CODE).append("ldc                   ").append(valueNode.getValue()).append('\n');
                        pushed();
                        break;

                    case DOUBLE_VALUE:
                        builder.append(TAB_CODE).append("ldc2_w                ").append(valueNode.getValue()).append('\n');
                        pushed();
                        pushed();
                        break;
                }

                typeOnStack = typeMap.get(valueNode.getValueType());

                break;

            case VARIABLE_GET:
                builder.append(loadVariableToStack(((VariableNode) atom).getVariableName()));
                break;

            case EXPRESSION:
                builder.append(compileExpression(atom));
                break;

            case METHOD_CALL:
                builder.append(compileMethodCall((MethodCallNode) atom));
                break;


        }

        return builder.toString();
    }

    private String compileMethodCall(MethodCallNode methodCall) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        MethodNode methodNode = methodMap.get(methodCall.getName());

        if (methodNode == null) {
            throw new CompilationErrorException(
                    "Method with name '" + methodCall.getName() + "' was not declared earlier");
        }

        builder.append(TAB_CODE).append("; METHOD CALL").append('\n');

        // push all parameters values to stack
        for (Node expression : methodCall.getParamsList().getChildren()) {
            builder.append(compileExpression(expression));
        }

        builder.append(TAB_CODE)
                .append("invokestatic          ")
                .append(constructMetodSignature(methodNode))
                .append("\n");

        return builder.toString();
    }

    private void createFile() throws IOException {
        Path path = Paths.get(OUT_FILE_PATH);

        Files.createDirectories(path.getParent());

        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException e) {
            System.err.println("already exists: " + e.getMessage());
        }
    }

    private void declareVariable(String name, ValueType type) {
        int index = getMinVariableIndex(type);

        Variable variable = new Variable(
                name,
                type,
                index
        );

        variableMap.put(variable.name, variable);

        localVariables[index] = 1;
        if (type == ValueType.D) {
            localVariables[index + 1] = 1;
        }
    }

    private String storeStackToVariable(String variableName) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        Variable variable = variableMap.get(variableName);

        builder.append(TAB_CODE);

        switch (variable.type) {
            case I:
                builder.append("i");
                break;
            case D:
                builder.append("d");
                break;
            default:
                throw new CompilationErrorException("values can be assigned only to integer and double type variables");
        }

        builder.append("store                ")
                .append(variable.index)
                .append("    ; store stack top to variable \"")
                .append(variable.name)
                .append("\"")
                .append("\n");

        stackDepth--;

        return builder.toString();
    }

    private String loadVariableToStack(String variableName) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        Variable variable = variableMap.get(variableName);

        builder.append(TAB_CODE);

        switch (variable.type) {
            case I:
                builder.append("i");
                break;
            case D:
                builder.append("d");
                break;
            default:
                throw new CompilationErrorException("values can be assigned only to integer and double type variables");
        }

        builder.append("load                 ")
                .append(variable.index)
                .append("    ; load variable \"")
                .append(variable.name)
                .append("\" to stack")
                .append("\n");

        typeOnStack = variable.type;

        pushed();

        return builder.toString();
    }

    private int getMinVariableIndex(ValueType type) {
        for (int i = 0; i < localVariables.length - 1; i++) {
            if (localVariables[i] == 0) {
                if (type == ValueType.D) {
                    if (localVariables[i + 1] == 0) {
                        return i;
                    } else {
                        continue;
                    }
                }
                return i;
            }
        }

        return -1;
    }

    private String constructMetodSignature(MethodNode methodNode) {
        StringBuilder result = new StringBuilder("MainJasmin/");

        result.append(methodNode.getName().getValue()).append("(");
        for (Node var : methodNode.getVarList().getChildren()) {
            result.append(typeMap.get(((VariableNode) var).getVariableType()));
        }

        result.append(")");
        result.append(typeMap.get(methodNode.getResultType()));

        return result.toString();
    }

    private void pushed() {
        stackDepth++;

        if (stackDepth > maxStackDepth) {
            maxStackDepth = stackDepth;
        }
    }

    private enum ValueType {
        I,
        D,
        V
    }

    private class Variable {
        private Variable(String name, ValueType type, int index) {
            this.name = name;
            this.type = type;
            this.index = index;
        }

        private String name;
        private ValueType type;
        private int index;
    }
}
