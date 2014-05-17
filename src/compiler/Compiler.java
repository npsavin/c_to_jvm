package compiler;

import parser.nodes.*;

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
            builder.append(CodeBuilder.CODE_HEADER);
            builder.append(CodeBuilder.CODE_CONSTRUCTOR);

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
        Arrays.fill(localVariables, 0);

        stackDepth = 0;
        maxStackDepth = 0;

        MethodNode methodNode = (MethodNode) node;

        methodMap.put(methodNode.getName().getValue(), methodNode);

        currentMethod = methodNode;

        String signature;
        // generate method signature
        if (methodNode.getName().getValue().equals("main")) {
            signature = CodeBuilder.METHOD_MAIN_SIGNATURE;
        } else {
            signature = CodeBuilder.methodDeclarationHeader(constructMethodDeclareSignature(methodNode));
        }

        List<Node> variables = methodNode.getVarList().getChildren();

        // declare all variables
        for (Node var : variables) {
            VariableNode variable = (VariableNode) var;
            declareVariable(variable.getVariableName(), typeMap.get(variable.getVariableType()));
        }

        // move arguments from stack to variables
//        for (int i = variables.size() - 1; i >= 0; i--) {
//            VariableNode var = (VariableNode) variables.get(i);
//
//            builder.append(loadVariableToStack(var.getVariableName()));
//        }

        // compile method commands
        for (Node command : methodNode.getBody().getChildren()) {
            builder.append(compileCommand(command));
        }

        // write return for void result type
        if (methodNode.getResultType() == ValueNode.ValueType.VOID_VALUE) {
            builder.append("   return\n");
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

        String limits = CodeBuilder.methodLimits(maxStackDepth, variablesAmount);

        String footer = CodeBuilder.METHOD_END;
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
                builder.append(CodeBuilder.CODE_INDENT)
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

        ValueType expected = typeMap.get(currentMethod.getResultType());

        if (!typeOnStack.equals(expected)) {
            builder.append(CodeBuilder.cast(typeOnStack, expected));
        }

        builder.append(CodeBuilder.returnOperation(expected));

        return builder.toString();
    }

    private String compilePrint(Node command) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        builder.append(CodeBuilder.CODE_INDENT)
                .append("getstatic             java/lang/System/out Ljava/io/PrintStream;\n");

        pushed(1);

        Node expression = command.getChild(0);

        if (expression != null) {
            builder.append(compileExpression(expression));
        }

        String type = typeOnStack.toString();

        builder.append(CodeBuilder.CODE_INDENT).append("invokevirtual         java/io/PrintStream/println(").append(type).append(")V\n");

        return builder.toString();
    }

    private String compileAssignValue(Node command) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        Node expression = command.getChild(0).getChild(0);

        builder.append(compileExpression(expression));

        Variable variable = variableMap.get(((VariableNode) command).getVariableName());

        if ( typeOnStack != variable.type ) {
            builder.append(CodeBuilder.cast(typeOnStack, variable.type));
        }

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

            CodeBuilder.StackOperation operation;

            switch (expression.getValueToken().getType()) {
                case PLUS:
                    operation = CodeBuilder.StackOperation.ADD;
                    break;
                case MINUS:
                    operation = CodeBuilder.StackOperation.SUB;
                    break;
                default:
                    throw new CompilationErrorException("Unknown operation with two parameters: " + expression.getValueToken().getType());
            }

            builder.append(compileTerm(expression.getChild(1)));
            ValueType secondType = typeOnStack;

            builder.append(compileBinaryStackOperation(firstType, secondType, operation));
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

            CodeBuilder.StackOperation operation;

            switch (term.getValueToken().getType()) {
                case MULTIPLY:
                    operation = CodeBuilder.StackOperation.MUL;
                    break;
                case DIVIDE:
                    operation = CodeBuilder.StackOperation.DIV;
                    break;
                default:
                    throw new CompilationErrorException("Unknown operation with two parameters: " + term.getValueToken().getType());
            }

            builder.append(compileTerm(term.getChild(1)));
            ValueType secondType = typeOnStack;

            builder.append(compileBinaryStackOperation(firstType, secondType, operation));
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
                builder.append(CodeBuilder.CODE_INDENT)
                        .append("i").append(operation).append("\n");
                typeOnStack = ValueType.I;
            } else {
                if (firstType == ValueType.D) {
                    // cast second argument ( stack top ) to double
                    builder.append(CodeBuilder.CODE_INDENT).append("i2d                   ; cast integer on stack to double\n");
                    popped(ValueType.I);
                    pushed(ValueType.D);
                } else if (secondType == ValueType.D) {
                    // cast first argument ( stack second ) to double
                    int index = getMinVariableIndex(ValueType.D);
                    builder.append(CodeBuilder.storeStackToVariable(ValueType.D, index, "temp"));
                    popped(ValueType.I);
                    builder.append(CodeBuilder.cast(ValueType.I, ValueType.D));
                    builder.append(CodeBuilder.loadVariableToStack(ValueType.D, index, "temp"));
                    pushed(ValueType.D);
                }

                builder.append(CodeBuilder.CODE_INDENT)
                        .append("d").append(operation).append("\n");
                typeOnStack = ValueType.D;

                popped(typeOnStack);
            }
        }

        return builder.toString();
    }

    private String compilePower(Node power) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        if (power.getNodeType() == Node.NodeType.UNARY_OPERATION) {
            Node atom = ((UnaryOperationNode) power).getOperand();
            builder.append(compileAtom(atom));

            String value;

            switch (typeOnStack) {
                case I:
                    value = "-1";
                    break;

                case D:
                    value = "-1.0";
                    break;

                default:
                    throw new CompilationErrorException("Cannot apply unary minus to unknown type " + typeOnStack);
            }

            builder.append(CodeBuilder.loadValueToStack(typeOnStack, value));
            pushed(typeOnStack);
            builder.append(CodeBuilder.operationOnStack(typeOnStack, CodeBuilder.StackOperation.MUL));
            popped(typeOnStack);

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

                String value = valueNode.getValue();
                ValueType type = typeMap.get(valueNode.getValueType());

                builder.append(CodeBuilder.loadValueToStack(type, value));
                pushed(type);

                typeOnStack = type;

                break;

            case VARIABLE_GET:
                builder.append(loadVariableToStack(((VariableNode) atom).getVariableName()));
                break;

            case UNARY_OPERATION:
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

        builder.append(CodeBuilder.CODE_INDENT).append("; METHOD CALL").append('\n');

        // push all parameters values to stack
        for (Node expression : methodCall.getParamsList().getChildren()) {
            builder.append(compileExpression(expression));
        }

        builder.append(CodeBuilder.CODE_INDENT)
                .append("invokestatic          ")
                .append(constructMethodCallSignature(methodNode))
                .append("\n");

        if (methodNode.getResultType() != ValueNode.ValueType.VOID_VALUE) {
            pushed(typeMap.get(methodNode.getResultType()));
            typeOnStack = typeMap.get(methodNode.getResultType());
        }

        return builder.toString();
    }

    private void createFile() throws IOException {
        Path path = Paths.get(OUT_FILE_PATH);

        Files.createDirectories(path.getParent());

        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException e) {
            System.err.println("Output file already exists (" + e.getMessage() + ") and will be overwritten");
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
        Variable variable = variableMap.get(variableName);
        String result = CodeBuilder.storeStackToVariable(variable);

        popped(variable.type);

        return result;
    }

    private String loadVariableToStack(String variableName) throws CompilationErrorException {
        Variable variable = variableMap.get(variableName);

        String result = CodeBuilder.loadVariableToStack(variable);

        typeOnStack = variable.type;

        pushed(typeOnStack);

        return result;
    }

    private String compileBinaryStackOperation(
            ValueType firstType,
            ValueType secondType,
            CodeBuilder.StackOperation operation) throws CompilationErrorException {
        StringBuilder builder = new StringBuilder();

        if (firstType == ValueType.I && secondType == ValueType.I) {
            // both values are integer, no need to cast
            builder.append(CodeBuilder.operationOnStack(ValueType.I, operation));
            typeOnStack = ValueType.I;
        } else {
            // one value is double - need double multiplication
            if (secondType == ValueType.I) {
                // cast second argument ( stack top ) to double
                builder.append(CodeBuilder.cast(ValueType.I, ValueType.D));
                popped(ValueType.I);
                pushed(ValueType.D);
            } else if (firstType == ValueType.I) {
                int index = getMinVariableIndex(ValueType.D);
                // cast first argument ( stack second ) to double
                builder.append(CodeBuilder.storeStackToVariable(ValueType.D, index, "temp"));
                popped(ValueType.I);
                builder.append(CodeBuilder.cast(ValueType.I, ValueType.D));
                builder.append(CodeBuilder.loadVariableToStack(ValueType.D, index, "temp"));
                pushed(ValueType.D);
            }

            builder.append(CodeBuilder.operationOnStack(ValueType.D, operation));
            typeOnStack = ValueType.D;
            popped(ValueType.D);
        }

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

    private String constructMethodCallSignature(MethodNode methodNode) {
        return "MainJasmin/" + constructMethodDeclareSignature(methodNode);
    }

    private String constructMethodDeclareSignature(MethodNode methodNode) {
        StringBuilder result = new StringBuilder("");

        result.append(methodNode.getName().getValue()).append("(");
        for (Node var : methodNode.getVarList().getChildren()) {
            result.append(typeMap.get(((VariableNode) var).getVariableType()));
        }

        result.append(")");
        result.append(typeMap.get(methodNode.getResultType()));

        return result.toString();
    }

    private void pushed(int size) {
        stackDepth += size;

        if (stackDepth > maxStackDepth) {
            maxStackDepth = stackDepth;
        }
    }

    private void pushed(ValueType type) throws CompilationErrorException {
        int size;

        switch (type) {
            case I:
                size = 1;
                break;

            case D:
                size = 2;
                break;

            default:
                throw new CompilationErrorException("Cannot push on stack item of unknown type: " + type);
        }

        pushed(size);
    }

    private void popped(int size) {
        stackDepth -= size;
    }

    private void popped(ValueType type) throws CompilationErrorException {
        int size;

        switch (type) {
            case I:
                size = 1;
                break;

            case D:
                size = 2;
                break;

            default:
                throw new CompilationErrorException("Cannot pop on stack item of unknown type: " + type);
        }

        popped(size);
    }

    public static enum ValueType {
        I,
        D,
        V
    }

    public static class Variable {
        private Variable(String name, ValueType type, int index) {
            this.name = name;
            this.type = type;
            this.index = index;
        }

        private String name;
        private ValueType type;
        private int index;

        public String getName() {
            return name;
        }

        public ValueType getType() {
            return type;
        }

        public int getIndex() {
            return index;
        }
    }
}
