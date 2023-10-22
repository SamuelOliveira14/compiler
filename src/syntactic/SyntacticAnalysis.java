package syntactic;
import java.io.FileNotFoundException;
import lexical.LexicalAnalysis;
import lexical.Token;
import lexical.TokenType;

public class SyntacticAnalysis {
    
    private LexicalAnalysis lexical;
    private Token current;
    
    public SyntacticAnalysis(String filePath){

        try{
            lexical = new LexicalAnalysis(filePath);
        }catch(FileNotFoundException exc){
            System.err.println("File " + filePath + " not found\n" + exc.getStackTrace());
            System.exit(1);
        }
        
    }

    private Token getToken(){

        try{
            return lexical.nextToken();
        }catch(Exception exc){
            System.err.println(exc.getMessage());
            System.exit(1);
        }
        
        return null;
    }

    private void advance(){
        this.current = this.getToken();
    }

    private void eat(TokenType type){
        if(current.getType() == type){
            advance();
        }else{
            error(type);
        }
    }

    private void error(TokenType type) {
        System.err.println("Unexpected token at line " + lexical.getLineCounter()+ ": expected " + type + " but got " + current.getType());
        System.exit(1);
    }
    private void error() {
        System.err.println("Unexpected token at line " + lexical.getLineCounter() + ": " + current.getType());
        System.exit(1);
    }

    public void start() {
        current = this.getToken();

        this.program();

        eat(TokenType.END_OF_FILE);
        System.out.println("Syntactic analysis completed with success!");
    }

    // program ::= "class" identifier [decl-list] body
    public void program() {
        switch(current.getType()){
            case CLASS:
                advance(); 
                eat(TokenType.IDENTIFIER);
                if(current.getType() == TokenType.INT || current.getType() == TokenType.FLOAT || current.getType() == TokenType.STRING){
                    declList();
                }
                body();
                break;
            default:
                error();
        }
    }

    // decl-list ::= decl ";" { decl ";" }
    public void declList() {
        decl();
        eat(TokenType.SEMI_COLON);

        while(current.getType() == TokenType.INT || current.getType() == TokenType.FLOAT || current.getType() == TokenType.STRING){
            decl();
            eat(TokenType.SEMI_COLON);
        }
    }

    // decl ::= type ident-list
    public void decl() {
        type();
        identList();
    }

    // ident-list ::= identifier {"," identifier}
    public void identList() {
        eat(TokenType.IDENTIFIER);
        while(current.getType() == TokenType.COMMA){
            eat(TokenType.COMMA);
            eat(TokenType.IDENTIFIER);
        }
    }

    // type ::= "int" | "string" | "float"
    public void type() {
        switch(current.getType()){
            case INT:
            case STRING:
            case FLOAT:
                advance();
                break;
            default: error();
        }
    }

    // body ::= "{" stmt-list "}"
    public void body() {
        eat(TokenType.OPEN_CUR); 
        stmtList();
        eat(TokenType.CLOSE_CUR);
    }

    // stmt-list ::= stmt ";" { stmt ";" }
    public void stmtList() {
        stmt();
        eat(TokenType.SEMI_COLON);

        while(current.getType() == TokenType.IDENTIFIER || 
                current.getType() == TokenType.IF || 
                current.getType() == TokenType.DO || 
                current.getType() == TokenType.READ || 
                current.getType() == TokenType.WRITE){
        
            stmt();
            eat(TokenType.SEMI_COLON);
        }
    }

    // stmt ::= assign-stmt | if-stmt | do-stmt | read-stmt | write-stmt
    public void stmt() {
        switch(current.getType()){
            case IDENTIFIER: assignStmt();
                break;
            case IF: 
                ifStmt();
                break;
            case DO: 
                doStmt();
                break;
            case READ: 
                readStmt();
                break;
            case WRITE: 
                writeStmt();
                break;
            default:
                error();
        }
    }

    // assign-stmt ::= identifier "=" simple_expr
    public void assignStmt() {
        eat(TokenType.IDENTIFIER); 
        eat(TokenType.ASSIGN); 
        simpleExpr();
    }

    // if-stmt ::= "if" "(" condition ")" "{" stmt-list "}" else-stmt
    public void ifStmt() {
        eat(TokenType.IF);
        eat(TokenType.OPEN_PAR);
        condition();
        eat(TokenType.CLOSE_PAR);
        eat(TokenType.OPEN_CUR);
        stmtList();
        eat(TokenType.CLOSE_CUR);
        elseStmt();
    }

    // else-stmt ::= "else" "{" stmt-list "}" | λ
    public void elseStmt() {
        switch(current.getType()){
            case ELSE:
                eat(TokenType.ELSE);
                eat(TokenType.OPEN_CUR);
                stmtList();
                eat(TokenType.CLOSE_CUR);
                break;
            default: return;
        }
    }

    // condition ::= expression
    public void condition() {
        expression();
    }

    // do-stmt ::= "do" "{" stmt-list "}" do-suffix
    public void doStmt() {
        eat(TokenType.DO);
        eat(TokenType.OPEN_CUR);
        stmtList();
        eat(TokenType.CLOSE_CUR);
        doSuffix();
    }

    // do-suffix ::= "while" "(" condition ")"
    public void doSuffix() {
        eat(TokenType.WHILE);
        eat(TokenType.OPEN_PAR);
        condition();
        eat(TokenType.CLOSE_PAR);
    }

    // read-stmt ::= "read" "(" identifier ")"
    public void readStmt() {
        eat(TokenType.READ);
        eat(TokenType.OPEN_PAR);
        eat(TokenType.IDENTIFIER);
        eat(TokenType.CLOSE_PAR);
    }

    // write-stmt ::= "write" "(" writable ")"
    public void writeStmt() {
        eat(TokenType.WRITE);
        eat(TokenType.OPEN_PAR);
        writable();
        eat(TokenType.CLOSE_PAR);
    }

    // writable ::= simple-expr
    public void writable() {
        simpleExpr();
    }

    // expression ::= simple-expr | simple-expr relop simple-expr
    public void expression() {
        simpleExpr(); exprPrime();
    }

    // expr_prime ::= relop simple_expr | λ
    public void exprPrime(){
        switch (current.getType()) {
            case GREATER:
            case GREATER_EQUAL:
            case LOWER:
            case LOWER_EQUAL:
            case NOT_EQUALS:
            case EQUALS:
                relop();
                simpleExpr();
                break;
            default: break;
        }
    }

    // simple-expr ::= term simple-expr-prime
    public void simpleExpr() {
        term();
        simpleExprPrime();
    }

    // simple-expr-prime ::= addop term simple-expr-prime | λ
    public void simpleExprPrime() {
        switch (current.getType()) {
            case ADD:
            case SUB:
            case OR:
                addop();
                term();
                simpleExprPrime();
            break;
            default: break;
        }
    }

    // term ::= factor-a term-prime
    public void term() {
        factorA(); 
        termPrime();
    }

    // term-prime ::= mulop factor-a term-prime | λ
    public void termPrime() {
        switch (current.getType()) {
            case MUL:
            case DIV:
            case AND:
                mulop();
                factorA();
                termPrime();
            break;
            default: break;
        }

    }

    // factor-a ::= factor | "!" factor | "-" factor
    public void factorA() {
        switch(current.getType()){
            case NOT:
                advance();
                factor();
            break;
            case SUB:
                advance();
                factor();
            break;
            case IDENTIFIER:
            case OPEN_PAR:
            case INTEGER_CONST:
            case LITERAL:
            case REAL_CONST:
                factor();
            break;

            default: error();
        }
    }

    // factor ::= identifier | constant | "(" expression ")"
    public void factor() {
        switch(current.getType()){
            case IDENTIFIER:
            case INTEGER_CONST:
            case LITERAL:
            case REAL_CONST:
                advance();
            break;
            case OPEN_PAR:
                advance();
                expression();
                eat(TokenType.CLOSE_PAR);
            break;
            default: error();
        }
    }

    // relop ::= ">" | ">=" | "<" | "<=" | "!=" | "=="
    public void relop() {
        switch (current.getType()) {
            case GREATER:
            case GREATER_EQUAL:
            case LOWER:
            case LOWER_EQUAL:
            case NOT_EQUALS:
            case EQUALS:
                advance();
            break;
            default: error();
        }
    }

    // addop ::= "+" | "-" | "||"
    public void addop() {
        switch (current.getType()) {
            case ADD:
            case SUB:
            case OR:
                advance();
            break;
            default: error();
        }
    }

    // mulop ::= "*" | "/" | "&&"
    public void mulop() {
        switch (current.getType()) {
            case MUL:
            case DIV:
            case AND:
                advance();
            break;
            default: error();
        }
    }

}
