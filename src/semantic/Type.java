package semantic;

public class Type {
    
    public static final int VOID = 0;
    public static final int INT = 1;
    public static final int FLOAT = 2;
    public static final int STRING = 3;
    public static final int BOOLEAN = 4;
    public static final int ERROR = -1;

    private int type;
    public Type(int type){
        if(type >= -1 && type <= 4){
            this.type = type;
        }else{
            this.type = ERROR;
        }
    }

    public int getType(){
        return this.type;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == this) return true;
        if (! (obj instanceof Type)) return false;
        
        Type t = (Type) obj;
        return this.type == t.getType();
    }

    public boolean equals(int idType){
        return idType == this.type;
    }

    @Override
    public String toString(){
        switch(this.type){
            case VOID: return "";
            case INT: return "int";
            case FLOAT: return "float";
            case STRING: return "string";
            case BOOLEAN: return "boolean";
            case ERROR:
            default: return "error";
        }
    }
}
