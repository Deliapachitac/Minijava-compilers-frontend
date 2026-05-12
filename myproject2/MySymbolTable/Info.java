package MySymbolTable;
import java.util.LinkedList;

public class Info {
    public String name;
    public boolean inherited;
    public String inherited_name; //null if the class does not inherit from another class (doesnt have the form class B extends A)

    public LinkedList<String[]> variables; // list of variables [type, name]
    public LinkedList<MethodInfo> methods; 

    public Info(String name, boolean inherited, String inherited_name) {
        
        this.name = name;
        this.inherited = inherited;
        this.inherited_name = inherited_name;


        variables = new LinkedList<String[]>();
        methods = new LinkedList<MethodInfo>();
    }


    public void addVariable(String type, String variablename)throws  Exception{
        // Check if the variable already exists in the current class
        for (String[] current : variables) {
            if (current[1].equals(variablename)) {
                throw new Exception("Variable already exists" );
            }
        }
        

        String[] new_variable =new String[]{type, variablename};
        variables.add(new_variable);
    }

    public void addMethod(String methodName, String returnType ,LinkedList<String[]> parameters)throws Exception {
        // Check if the method already exists in the current class
        String key =MethodInfo.getKey(methodName, parameters);
        for (MethodInfo current : methods){
            if (current.getKey().equals(key)){
                throw new Exception("Method already exists" );    
                  
           }
        }             

        MethodInfo new_method =new MethodInfo(methodName, returnType);
        methods.add(new_method);
    }


}


class MethodInfo {
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