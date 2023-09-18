package lexical;

public class WordToken extends Token {
    
    private String lexeme;

    public WordToken(TokenType type, String literal){
        super(type);
        this.lexeme = literal;
    }

    @Override
    public String toString(){
        return super.toString() + " [" +lexeme+ "]";
    }

    public String getLexeme(){
        return lexeme;
    }
}
