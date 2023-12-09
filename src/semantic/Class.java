package semantic;

public class Class {
    public static final int CLASS = 1;
    public static final int VARIABLE = 2;
    public static final int ERROR = -1;

    private int idClass;
    public Class(int idClass){
        if(idClass >= 1 && idClass <= 2){
            this.idClass = idClass;
        }else{
            this.idClass = ERROR;
        }
    }

    public int getIdClass(){
        return this.idClass;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(!(obj instanceof Class)) return false;
        
        Class c = (Class) obj;
        return this.idClass == c.getIdClass();
    }

    public boolean equals(int idClass){
        return idClass == this.idClass;
    }

    @Override
    public String toString(){
        switch(this.idClass){
            case CLASS: return "class";
            case VARIABLE: return "variable";
            default: return "error";
        }
    }
}
