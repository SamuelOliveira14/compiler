package lexical;

public class FloatToken extends Token {
    
    private float value;

    public FloatToken(float value){
        super(TokenType.REAL_CONST);
        this.value = value;
    }

    @Override
    public String toString(){
        return super.toString() + " [" +Float.toString(value)+ "]";
    }
}
