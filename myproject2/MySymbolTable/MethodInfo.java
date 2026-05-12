package MySymbolTable;
import java.util.LinkedList;

public class MethodInfo {
    public String name;
    public String returnType;
    public LinkedList<String[]> parameters; // list of parameters [type, name]
    public LinkedList<String[]> localVariables; // list of local variables [type, name]

    public MethodInfo(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;

        parameters = new LinkedList<String[]>();
        localVariables = new LinkedList<String[]>();
        
    }

    public void addParameter(String type, String parameterName)throws Exception{
        // Check if the parameter already exists in the current method
        for (String[] current : parameters) {
            if (current[1].equals(parameterName)) {
                throw new Exception("Parameter already exists" );
              
            }
        }         

        String[] new_parameter = new String[]{type, parameterName};
        parameters.add(new_parameter);
    }

    public void addLocalVariable(String type, String variableName)throws Exception {
        // Check if the local variable already exists in the current method
        for (String[] current : localVariables) {
            if (current[1].equals(variableName)) {
                throw new Exception("Local variable already exists" );
                
            }
        }         

        String[] new_local_variable = new String[]{type, variableName};
        localVariables.add(new_local_variable);
    }

    
    public static String getKey(String name, LinkedList<String[]> parameters) {
        String key= new String(name);
        for (String[] p: parameters) {
            key += "_" + p[0]; 
        }
        return key;
    }
    public String getKey(){
        return getKey(this.name,this.parameters);
    }      
}
