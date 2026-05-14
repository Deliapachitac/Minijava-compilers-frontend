package MySymbolTable;


/* This class store information about a single field of a class */
public class FieldInfo {
    private String type;
    private String name;
    private int offset;

    public  FieldInfo(String type,String name,int offset){
        this.type= type;
        this.name= name;   
        this.offset =offset ;
    }      

    public String getType(){
        return type;   
    }
                
    public String getName()  {
        return  name;
    }    

    public  int getOffset(){
        return  offset;
    }
          
}
