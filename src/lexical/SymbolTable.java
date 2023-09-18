package lexical;

import java.util.HashMap;

public class SymbolTable {

    private class Columns{
        //ST columns:
        public String type;

        public Columns(String s){
            type = s;
        }
    }

    private HashMap<String,Columns> symbolTable;

    public SymbolTable(){
        symbolTable = new HashMap<String,Columns>();
    }

    public void put(WordToken token, String type){
        Columns info = new Columns(type);
        symbolTable.put(token.getLexeme(), info);
    }

    public void put(WordToken token){
        Columns info = new Columns(" ");
        symbolTable.put(token.getLexeme(), info);
    }

    public void update(WordToken token, String type){
        Columns info = symbolTable.get(token.getLexeme());
        info.type = type;
        symbolTable.replace(token.getLexeme(), info);
    }

    public String toString(){
        String s = "\nSymbol Table:\nID \t| Type \t|\n+---------------+\n";
        for(String lexeme : symbolTable.keySet()){
            s += "" + lexeme + "\t|" + symbolTable.get(lexeme).type + "\t|\n";
        }
        return s;
    }

}
