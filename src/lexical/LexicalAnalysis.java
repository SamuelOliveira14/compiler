package lexical;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;

public class LexicalAnalysis {

    private PushbackInputStream file;
    private int lineCounter;
    private int currentChar;
    public SymbolTable st;

    public LexicalAnalysis(String filePath) throws FileNotFoundException{
        this.lineCounter = 1;
        this.currentChar = ' ';
        st = new SymbolTable();

        try{
            this.file = new PushbackInputStream(new FileInputStream(filePath), 2);
        }catch(Exception e){
            throw new FileNotFoundException(filePath);
        }
    }


    public Token nextToken() throws EOFException{

        int state = 0;
        final int ERROR = -10;
        final int END = -15;
        Token token = null;
        String lexeme = new String("");

        while(state != END && state != ERROR){
            
            this.currentChar = getc();
            
            //DEBUG:
            //System.out.printf("  [%d, %d ('%c')]\n", state, currentChar, (char) currentChar);

            switch(state){
                case 0:
                    if(currentChar == -1){
                        token = new Token(TokenType.END_OF_FILE);
                        state = END;
                        break;
                    }else if(currentChar == '\n'){
                        this.lineCounter++;
                        state = 0;
                    }else if(currentChar == ' ' || (char) currentChar == '\t' ||
                             currentChar == 13){
                        state = 0;
                    }else if(Character.isLetter(currentChar)){
                        lexeme += (char) currentChar;
                        state = 1;
                    }else if(currentChar == '0'){
                        lexeme += (char) currentChar;
                        state = 2;
                    }else if(Character.isDigit(currentChar)){
                        lexeme += (char) currentChar;
                        state = 3;
                    }else if(currentChar == '\"'){
                        //lexeme += (char) currentChar;
                        state = 5;
                    }else if(currentChar == '&'){
                        lexeme += (char) currentChar;
                        state = 6;
                    }else if(currentChar == '/'){
                        lexeme += (char) currentChar;
                        state = 7;
                    }else if(currentChar == '|'){
                        lexeme += (char) currentChar;
                        state = 11;
                    }else if(currentChar == '<' || currentChar == '>' || 
                             currentChar == '=' || currentChar == '!'){ 
                        lexeme += (char) currentChar;
                        state = 12;
                    }else if(currentChar == '(' || currentChar == ')' || 
                             currentChar == '{' || currentChar == '}' || 
                             currentChar == ';' || currentChar == '+' || 
                             currentChar == '-' || currentChar == '*' || currentChar == ','){
                        lexeme += (char) currentChar;
                        token = new Token(TokenType.getLexemeType(lexeme));
                        state = END;
                    }else{
                        state = ERROR;
                    }
                break;
                case 1:
                    if(Character.isLetterOrDigit(currentChar) || currentChar == '_'){
                        lexeme += (char) currentChar;
                        state = 1;
                    }else{
                        unget(currentChar);
                        TokenType type = TokenType.getLexemeType(lexeme);
                        if(type != null){ //Verify if this token is a keyword.
                            token = new Token(type);
                        }else{
                            type = TokenType.IDENTIFIER;
                            token = new WordToken(type, lexeme);
                            //TODO: Put into ST

                            st.put((WordToken) token);
                        }
                        state = END;
                    }
                break;
                case 2:
                    if(currentChar == '.'){
                        lexeme += (char) currentChar;
                        state = 4;
                    }else{
                        unget(currentChar);
                        token = new IntegerToken(Integer.parseInt(lexeme));
                        state = END;
                    }
                break;
                case 3:
                    if(Character.isDigit(currentChar)){
                        lexeme += (char) currentChar;
                        state = 3;
                    }else if(currentChar == '.'){
                        lexeme += (char) currentChar;
                        state = 4;
                    }else{
                        unget(currentChar);
                        token = new IntegerToken(Integer.parseInt(lexeme));
                        state = END;
                    }
                break;
                case 4:
                    if(Character.isDigit(currentChar)){
                        lexeme += (char) currentChar;
                        state = 4;
                    }else{
                        unget(currentChar);
                        token = new FloatToken(Float.parseFloat(lexeme));
                        state = END;
                    }
                break;
                case 5:
                    if(currentChar == '\"'){
                        //lexeme += (char) currentChar;
                        token = new WordToken(TokenType.LITERAL, lexeme);
                        state = END;
                    }else{
                        lexeme += (char) currentChar;
                        state = 5;
                    }
                
                break;
                case 6:
                    if(currentChar == '&'){
                        token = new Token(TokenType.AND);
                        state = END;
                    }else{
                        unget(currentChar);
                        state = ERROR;
                    }
                break;
                case 7:
                    if(currentChar == '*'){
                        state = 9;
                    }else if(currentChar == '/'){
                        state = 8;
                    }else{
                        unget(currentChar);
                        token = new Token(TokenType.DIV);
                        state = END;
                    }
                break;
                case 8:
                    if(currentChar == '\n'){
                        lexeme = "";
                        this.lineCounter++;
                        state = 0;
                    }else{
                        state = 8;
                    }
                break;
                case 9:
                    if(currentChar == '*'){
                        state = 10;
                    }else if(currentChar == '\n'){
                        this.lineCounter++;
                        state = 9;
                    }else{
                        state = 9;
                    }
                break;
                case 10:
                    if(currentChar == '/'){
                        lexeme = "";
                        state = 0;
                    }else if(currentChar == '*'){
                        state = 10;
                    }else{
                        state = 9;
                    }
                break;
                case 11:
                    if(currentChar == '|'){
                        token = new Token(TokenType.OR);
                        state = END;
                    }else{
                        unget(currentChar);
                        state = ERROR;
                    }
                break;
                case 12:
                    if(currentChar == '='){
                        lexeme += (char) currentChar;
                        token = new Token(TokenType.getLexemeType(lexeme));
                        state = END;
                    }else{
                        unget(currentChar);
                        token = new Token(TokenType.getLexemeType(lexeme));
                        state = END;
                    }
                break;
                default:
                    state = ERROR;
            }

            if(currentChar == -1 && state != END && state != ERROR){
                token = new Token(TokenType.UNEXPECTED_EOF);
                throw new EOFException("Unexpected END OF FILE at line " + getLineCounter() +" near '" + 
                                       (lexeme.isEmpty() ? (char) currentChar : lexeme) + "': " + token.type);
                
            }

        }

        if(state == END){
            return token;
        }else if(state == ERROR){
            token = new Token(TokenType.INVALID_TOKEN);
            throw new RuntimeException("Unexpected Token at line " + getLineCounter() +" near '" +
                                       (lexeme.isEmpty() ? (char) currentChar : lexeme) + "': " + token.type);
        }
        
        return null;
    }

    public void close() throws IOException{
        file.close();
    }

    public int getc(){
        try{
            return file.read();
        }catch(Exception e){
            throw new RuntimeException("Unable to read file");
        }
    }

    private void unget(int c){
        try {
            file.unread(c);
        } catch (Exception e) {
            throw new RuntimeException("Unable to unget " + (char) c);
        }
    }

    public int getLineCounter() {
        return lineCounter;
    }
}