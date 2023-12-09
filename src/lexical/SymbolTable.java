package lexical;

import semantic.Type;
import semantic.Class;
import java.util.HashMap;


public class SymbolTable {

    private class Columns{
        //ST columns:
        public Type type;
        public Class idClass;
        public int offset;

        public Columns(){
            type = null;
            idClass = null;
            offset = 0;
        }
    }

    private HashMap<String,Columns> symbolTable;

    public SymbolTable(){
        symbolTable = new HashMap<String,Columns>();
    }

    public void put(WordToken token){
        if(symbolTable.get(token.getLexeme()) == null) symbolTable.put(token.getLexeme(), new Columns());
    }

    public void updateType(WordToken token, Type type){
        Columns info = symbolTable.get(token.getLexeme());
        info.type = type;
        symbolTable.replace(token.getLexeme(), info);
    }

    public void updateClass(WordToken token, Class idClass){
        Columns info = symbolTable.get(token.getLexeme());
        info.idClass = idClass;
        symbolTable.replace(token.getLexeme(), info);
    }

    public Type getType(WordToken token){
        Columns c = symbolTable.get(token.getLexeme());
        return c.type;
    }

    public Class getClass(WordToken token){
        Columns c = symbolTable.get(token.getLexeme());
        return c.idClass;
    }

}
