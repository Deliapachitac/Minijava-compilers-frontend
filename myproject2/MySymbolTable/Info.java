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
        for (String[] p: parameters) {
            new_method.addParameter(p[0], p[1]);
        }
        methods.add(new_method);
    }


}


