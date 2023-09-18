package lexical;

public class IntegerToken extends Token {
    
    private int value;

    public IntegerToken(int value){
        super(TokenType.INTEGER_CONST);
        this.value = value;
    }

    @Override
    public String toString(){
        return super.toString() + " [" +Integer.toString(value)+ "]";
    }
}

