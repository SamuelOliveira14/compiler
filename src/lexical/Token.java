package lexical;

public class Token {
    
    protected TokenType type;

    public Token(TokenType type){
        this.type = type;
    }

    public TokenType getType() {
        return type;
    }

    public String toString(){
        return type.getLexeme() == "" ? type + "" : type + " " + "\"" +type.getLexeme() + "\"";
    }

}
