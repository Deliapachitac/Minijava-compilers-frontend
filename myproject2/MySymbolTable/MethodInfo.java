package MySymbolTable;
import java.util.LinkedList;



/* Information about a method in a class*/
public class MethodInfo {
    private String name;
    private String returnType;
    private LinkedList<String[]> parameters; // list of parameters [type, name]
    private int offset; // offset of the method in the vtable


    public MethodInfo(String name, String returnType, int offset) {
        this.name= name;
        this.returnType= returnType;
        this.offset = offset;
        this.parameters = new LinkedList<>();

    }

    public void addParameter(String type, String parameterName){
                     
        String[] new_parameter= new String[]{type, parameterName} ;
        this.parameters.add(new_parameter);
    }    

    public String getName(){
        return  name;
    }  

    public String getReturnType() {
        return returnType;
    }

    public LinkedList<String[]> getParameters() {
        return parameters;
    }

    public int getOffset() {
        return offset;
    }

    //Helpful function for returning only the types of the parameters
    public LinkedList<String> getParameterTypes() {
        LinkedList<String> parameterTypes = new LinkedList<>();
        for (String[] parameter : parameters) {
            parameterTypes.add(parameter[0]);
        }
        return parameterTypes;
    }


}
