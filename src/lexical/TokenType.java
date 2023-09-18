package lexical;

public enum TokenType {
    // SPECIALS
    UNEXPECTED_EOF(""),
    INVALID_TOKEN(""),
    END_OF_FILE(""),


    // SYMBOLS
    SEMI_COLON(";"),     // ;
    COLON(":"),          // :
    COMMA(","),          // ,
    DOT("."),            // .
    OPEN_PAR("("),       // (
    CLOSE_PAR(")"),      // )
    OPEN_CUR("{"),       // {
    CLOSE_CUR("}"),      // }
    OPEN_BRA("["),       // [
    CLOSE_BRA("]"),      // ]

    // OPERATORS
    ASSIGN("="),         // =
    AND("&&"),           // &&
    OR("||"),            // ||
    LOWER("<"),          // <
    GREATER(">"),        // >
    LOWER_EQUAL("<="),   // <=
    GREATER_EQUAL(">="), // >=
    EQUALS("=="),        // ==
    NOT_EQUALS("!="),    // !=
    ADD("+"),            // +
    SUB("-"),            // -
    MUL("*"),            // *
    DIV("/"),            // /
    NOT("!"),            // !

// KEYWORDS
    CLASS("class"), 
    IF("if"),            // if
    ELSE("else"),        // else
    DO("do"),            // do
    WHILE("while"),      // while
    INT("int"),          // int tpye
    FLOAT("float"),      // float type
    STRING("string"),    // String
    NULL("null"),        // null
    FALSE("False"),      // false
    TRUE("True"),        // true
    READ("read"),        // read
    WRITE("write"),      // write


    // OTHERS
    IDENTIFIER(""),      // identifier
    INTEGER_CONST(""),         // integer
    REAL_CONST(""),            // float
    LITERAL("");         // string
    
    final String lexeme;

    private TokenType(String lex){
        this.lexeme = lex;
    }

    public static TokenType getLexemeType(String lexeme){
        for (TokenType type : TokenType.values()){
            if(type.lexeme.equals(lexeme)){
                return type;
            }
        }

        return null;
    }

    public String getLexeme(){
        return lexeme;
    }
};
