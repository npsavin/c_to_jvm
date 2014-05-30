package parser.nodes;

public enum NodeType {
    VALUE,
    INT_VALUE,
    DOUBLE_VALUE,
    OPERATION,
    LIST,
    BODY,
    PROGRAM,
    ASSIGNED,
    METHOD_CALL,
    RETURN,
    PRINT,
    DECLARE,
    TERM,
    FACTOR,
    POWER,
    EXPRESSION,
    ATOM,
    VARIABLE_GET,
    UNARY_OPERATION,

    CONDITIONAL_CONSTRUCTION,
    IF_BLOCK,
    ELSE_IF_BLOCK,
    ELSE_BLOCK,
}
