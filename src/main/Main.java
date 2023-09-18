package main;
import lexical.LexicalAnalysis;
import lexical.Token;
import lexical.TokenType;

public class Main {
    public static void main(String[] args) {

        if (args.length < 1){
            System.out.println("Usage: java Main [source code path]");
            System.exit(1);
        }

        String path = args[0];
        Token token;
        
        try{
            LexicalAnalysis lex = new LexicalAnalysis(path);

            do{
                token = lex.nextToken();
                System.out.println(token.toString());
            }while(token.getType() != TokenType.END_OF_FILE && token.getType() != TokenType.INVALID_TOKEN);
            
            lex.close();
            System.out.println(lex.st.toString());

        }catch(Exception e){
            //System.err.println("Internal error " + (e.getMessage() == null ? "" : e.getMessage()));
            e.printStackTrace();
            System.exit(1);
        }
    }
}
