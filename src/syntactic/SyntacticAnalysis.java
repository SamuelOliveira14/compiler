package syntactic;
import java.io.FileNotFoundException;
import lexical.LexicalAnalysis;
import lexical.SymbolTable;
import lexical.Token;
import lexical.TokenType;
import lexical.WordToken;
import semantic.Class;
import semantic.Type;

public class SyntacticAnalysis {
    
    private LexicalAnalysis lexical;
    private Token previous;
    private Token current;
    private SymbolTable st;

    public SyntacticAnalysis(String filePath){

        st = new SymbolTable();
        try{
            lexical = new LexicalAnalysis(filePath, st);
        }catch(FileNotFoundException e){
            System.err.println("File " + filePath + " not found\n" + e.getStackTrace());
            System.exit(1);
        }
        
    }

    private Token getToken(){

        try{
            return lexical.nextToken();
        }catch(Exception e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
        return null;
    }

    private void advance(){
        previous = current;
        this.current = this.getToken();
    }

    private void eat(TokenType type){
        if(current.getType() == type){
            advance();
        }else{
            error(type);
        }
    }

    private void updateIdentifier(WordToken wToken, Type idType, Class idClass){

        if(idType.equals(Type.ERROR)) error("Type error");
        if(idClass.equals(Class.ERROR)) error("Class error");

        st.updateType(wToken, idType);
        st.updateClass(wToken, idClass);
    }

    private void checkDeclarationUniqueness(WordToken wToken){
        if(!(st.getClass(wToken) == null && st.getType(wToken) == null)){
            error("Identifier \'" + wToken.getLexeme() + "\' is already defined.");
        }
    }

    private void checkUsageUniqueness(WordToken wToken){
        if(st.getClass(wToken) == null && st.getType(wToken) == null){
            error("Identifier \'" + wToken.getLexeme() + "\' is not defined.");
        }
    }

    private void checkClassCompatibility(WordToken wToken, int idClass){
        if(!(st.getClass(wToken).equals(idClass))){
            error(wToken.getLexeme() + " is not a " + new Class(idClass));
        }
    }

    private boolean compatibleTypes(Type t1, Type t2){
        if(t1.equals(Type.VOID)) return true;
        if(t2.equals(Type.VOID)) return true;
        if(t1.equals(t2)) return true;

        return false;
    }

    private void error(TokenType type) {
        System.err.println("Unexpected token at line " + lexical.getLineCounter()+ ": expecting " + type.getLexeme() + " but got " + current.getType().getLexeme());
        System.exit(1);
    }
    private void error() {
        System.err.println("Unexpected token at line " + lexical.getLineCounter() + ": " + current.getType().getLexeme());
        System.exit(1);
    }
    private void error(String message){
        System.err.println("Error at line " + lexical.getLineCounter() + ": " + message);
        System.exit(1);
    }

    public void start() {
        current = this.getToken();

        this.program();

        eat(TokenType.END_OF_FILE);
        System.out.println("Semantic analysis completed with success!");
    }

    // program ::= "class" identifier [decl-list] body
    private void program() {
        switch(current.getType()){
            case CLASS:
                advance(); 
                eat(TokenType.IDENTIFIER);

                checkDeclarationUniqueness((WordToken) previous);
                updateIdentifier((WordToken) previous, new Type(Type.VOID), new Class(Class.CLASS) ); // {identifier.type = VOID} {identifier.class = CLASS}

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
    private void declList() {
        decl();
        eat(TokenType.SEMI_COLON);

        while(current.getType() == TokenType.INT || current.getType() == TokenType.FLOAT || current.getType() == TokenType.STRING){
            decl();
            eat(TokenType.SEMI_COLON);
        }
    }

    // decl ::= type ident-list
    private void decl() {
        Type idType = type();
        identList(idType);
    }

    // ident-list ::= identifier {"," identifier}
    private void identList(Type idType) {
        eat(TokenType.IDENTIFIER);

        checkDeclarationUniqueness((WordToken) previous);
        updateIdentifier((WordToken) previous, idType, new Class(Class.VARIABLE));

        while(current.getType() == TokenType.COMMA){
            eat(TokenType.COMMA);
            eat(TokenType.IDENTIFIER);

            checkDeclarationUniqueness((WordToken) previous);
            updateIdentifier((WordToken) previous, idType, new Class(Class.VARIABLE));
        }
    }

    // type ::= "int" | "string" | "float"
    private Type type() {
        switch(current.getType()){
            case INT: advance(); return new Type(Type.INT);
            case STRING: advance(); return new Type(Type.STRING);
            case FLOAT: advance(); return new Type(Type.FLOAT);
            default: error(); return new Type(Type.ERROR);
        }
    }

    // body ::= "{" stmt-list "}"
    private void body() {
        eat(TokenType.OPEN_CUR); 
        stmtList();
        eat(TokenType.CLOSE_CUR);
    }

    // stmt-list ::= stmt ";" { stmt ";" }
    private void stmtList() {
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
    private void stmt() {
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
    private void assignStmt() {
        eat(TokenType.IDENTIFIER);
        
        checkUsageUniqueness((WordToken) previous);
        checkClassCompatibility((WordToken) previous, Class.VARIABLE);

        Type idType = st.getType((WordToken) previous);
        eat(TokenType.ASSIGN); 
        Type simpleExprType = simpleExpr();

        if(!idType.equals(simpleExprType)) error("incompatible types: " + simpleExprType + " cannot be assigned to " + idType);
    }

    // if-stmt ::= "if" "(" condition ")" "{" stmt-list "}" else-stmt
    private void ifStmt() {
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
    private void elseStmt() {
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
    private void condition() {
        Type exprType = expression();
        if(!exprType.equals(Type.BOOLEAN)) error("condition must be a boolean value");
    }

    // do-stmt ::= "do" "{" stmt-list "}" do-suffix
    private void doStmt() {
        eat(TokenType.DO);
        eat(TokenType.OPEN_CUR);
        stmtList();
        eat(TokenType.CLOSE_CUR);
        doSuffix();
    }

    // do-suffix ::= "while" "(" condition ")"
    private void doSuffix() {
        eat(TokenType.WHILE);
        eat(TokenType.OPEN_PAR);
        condition();
        eat(TokenType.CLOSE_PAR);
    }

    // read-stmt ::= "read" "(" identifier ")"
    private void readStmt() {
        eat(TokenType.READ);
        eat(TokenType.OPEN_PAR);
        eat(TokenType.IDENTIFIER);
        checkUsageUniqueness((WordToken) previous);
        checkClassCompatibility((WordToken) previous, Class.VARIABLE);
        eat(TokenType.CLOSE_PAR);
    }

    // write-stmt ::= "write" "(" writable ")"
    private void writeStmt() {
        eat(TokenType.WRITE);
        eat(TokenType.OPEN_PAR);
        writable();
        eat(TokenType.CLOSE_PAR);
    }

    // writable ::= simple-expr
    private void writable() {
        simpleExpr();
    }

    // expression ::= simple-expr | simple-expr relop simple-expr
    // expression ::= simple_expr expr_prime
    private Type expression() {
        Type simpleExprType = simpleExpr(); 
        Type exprPrimeType = exprPrime();

        if(exprPrimeType.equals(Type.VOID)) 
            return simpleExprType;
        else if(simpleExprType.equals(exprPrimeType)) 
            return new Type(Type.BOOLEAN);

        error("bad operand types (" + simpleExprType + " and " + exprPrimeType + ") for binary relational operator");

        return null;
    }

    // expr_prime ::= relop simple_expr | λ
    private Type exprPrime(){
        Type simpleExprType;
        switch (current.getType()) {
            case GREATER:
            case GREATER_EQUAL:
            case LOWER:
            case LOWER_EQUAL:
                relop();
                simpleExprType = simpleExpr();
                if(!simpleExprType.equals(Type.STRING) && !simpleExprType.equals(Type.BOOLEAN)) return simpleExprType;
                error("bad operand type (" + simpleExprType + ") for binary relational operator");
            break;
            case NOT_EQUALS:
            case EQUALS:
                relop();
                return simpleExpr();
            default: break;
        }
        return new Type(Type.VOID);
    }

    // simple-expr ::= term simple-expr-prime
    private Type simpleExpr() {
        Type termType = term();
        Type simpleExprPrimeType = simpleExprPrime(termType);
        
        if(compatibleTypes(termType, simpleExprPrimeType)) 
            return termType;
        
        error("incompatible types: " + termType + (simpleExprPrimeType.equals(Type.VOID) ? "" : " and " + simpleExprPrimeType));
        return null;
    }

    // simple-expr-prime ::= addop term simple-expr-prime | λ
    private Type simpleExprPrime(Type leftType) {
        Type termType = null;
        Type simpleExprPrimeType = null;
        Type resultType = null;
        switch (current.getType()) {
            case ADD:
                addop();
                termType = term();

                if(!leftType.equals(termType) || leftType.equals(Type.BOOLEAN))
                    error("incompatible types for '+' operator: " + leftType + " and " + termType);

                resultType = leftType;
                simpleExprPrimeType = simpleExprPrime(resultType);

                if(compatibleTypes(resultType, simpleExprPrimeType)) 
                    return resultType;
                
                error("incompatible types: " + resultType + " and " + simpleExprPrimeType);

            break;
            case SUB:
                addop();
                termType = term();

                if(!leftType.equals(termType) || leftType.equals(Type.BOOLEAN) || leftType.equals(Type.STRING))
                    error("incompatible types for '-' operator: " + leftType + " and " + termType);

                resultType = leftType;
                simpleExprPrimeType = simpleExprPrime(resultType);

                if(compatibleTypes(resultType, simpleExprPrimeType)) 
                    return resultType;

                error("incompatible types: " + resultType + " and " + simpleExprPrimeType);
            break;
            case OR:
                addop();
                termType = term();

                if(!leftType.equals(termType) || !leftType.equals(Type.BOOLEAN))
                    error("incompatible types for '||' operator: " + leftType + " and " + termType);

                resultType = leftType;
                simpleExprPrimeType = simpleExprPrime(resultType);

                if(compatibleTypes(resultType, simpleExprPrimeType)) 
                    return resultType;

                error("incompatible types: " + resultType + " and " + simpleExprPrimeType);
            break;
            default: break;
        }
        return new Type(Type.VOID);
    }

    // term ::= factor-a term-prime
    private Type term() {
        Type factorAType = factorA(); 
        Type termPrimeType = termPrime(factorAType);

        return termPrimeType.equals(Type.VOID) ? factorAType : termPrimeType;
    }

    // term-prime ::= mulop factor-a term-prime | λ
    private Type termPrime(Type leftType) {
        Type factorAType = null;
        Type termPrimeType = null;
        Type resultType = null;
        switch (current.getType()) {
            case MUL:
                mulop();
                factorAType = factorA();

                if(!leftType.equals(factorAType) || leftType.equals(Type.BOOLEAN) || leftType.equals(Type.STRING)) 
                    error("incompatible types for '*' operator: " + leftType + " and " + factorAType);

                resultType = leftType;
                termPrimeType = termPrime(resultType);

                if(compatibleTypes(resultType, termPrimeType)) 
                    return resultType;

                error("incompatible types: " + resultType + " and " + termPrimeType);
            break;
            case DIV:
                mulop();
                factorAType = factorA();
                
                if(!leftType.equals(factorAType) || leftType.equals(Type.BOOLEAN) || leftType.equals(Type.STRING)) 
                    error("incompatible types for '/' operator: " + leftType + " and " + factorAType);
                
                resultType = new Type(Type.FLOAT);

                termPrimeType = termPrime(resultType);

                if(compatibleTypes(resultType, termPrimeType)) 
                    return resultType;

                error("incompatible types" + resultType + " and " + termPrimeType);
            break;
            case AND:
                mulop();
                factorAType = factorA();

                if(!leftType.equals(factorAType) || !leftType.equals(Type.BOOLEAN))
                    error("incompatible types for '&&' operator: " + leftType + " and " + factorAType);

                resultType = leftType;
                termPrimeType = termPrime(resultType);

                if(resultType.equals(Type.BOOLEAN) && termPrimeType.equals(Type.BOOLEAN) || resultType.equals(Type.BOOLEAN) && termPrimeType.equals(Type.VOID))
                    return new Type(Type.BOOLEAN);
                    
                error("bad operand types for binary operator '&&'");
            break;

            default: break;
        }
        return new Type(Type.VOID);
    }

    // factor-a ::= factor | "!" factor | "-" factor
    private Type factorA() {
        Type factorType = null;
        switch(current.getType()){
            case NOT:
                advance();
                factorType = factor();
                if(factorType.equals(Type.BOOLEAN)) break;
                error("bad operand type "+ factorType +" for unary operator '!'");
            break;
            case SUB:
                advance();
                factorType = factor();
                if(factorType.equals(Type.FLOAT) || factorType.equals(Type.INT)) break;
                error("bad operand type "+ factorType +" for unary operator '-'");
            break;
            case IDENTIFIER:
            case OPEN_PAR:
            case INTEGER_CONST:
            case LITERAL:
            case REAL_CONST:
                factorType = factor();
            break;

            default: error();
        }

        return factorType;
    }

    // factor ::= identifier | constant | "(" expression ")"
    private Type factor() {
        switch(current.getType()){
            case IDENTIFIER:
                advance();
                checkUsageUniqueness((WordToken) previous);
                checkClassCompatibility((WordToken) previous,Class.VARIABLE);
                return st.getType((WordToken) previous);
            case INTEGER_CONST:
                advance();
                return new Type(Type.INT);
            case LITERAL:
                advance();
                return new Type(Type.STRING);
            case REAL_CONST:
                advance();
                return new Type(Type.FLOAT);
            case OPEN_PAR:
                advance();
                Type exprType = expression();
                eat(TokenType.CLOSE_PAR);
                return exprType;
            default: error(); return null;
        }
    }

    // relop ::= ">" | ">=" | "<" | "<=" | "!=" | "=="
    private void relop() {
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
    private void addop() {
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
    private void mulop() {
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
