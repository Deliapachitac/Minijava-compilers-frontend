package MySymbolTable;
import java.util.*;


/* Stores information for a single class */

public class ClassInfo {
    private String name;
    private ClassInfo parentClass; // class B extends A => parentClass of B is A   
    private  Map<String, FieldInfo>  fields= new LinkedHashMap<>(); 
    private  Map<String, LinkedList<MethodInfo>> methods= new LinkedHashMap<>();

    private int fieldOffset; 
    private int methodOffset; 

    public ClassInfo(String name,ClassInfo parentClass) {
        
        this.name = name;
        this.parentClass = parentClass;
        this.fieldOffset = 0;
        this.methodOffset = 0;

        //the offsets begin where the parent class left off
        if (parentClass != null) {
            this.fieldOffset = parentClass.getFieldOffset();
            this.methodOffset = parentClass.getMethodOffset();
        }

    }


    public boolean addField(String type, String variablename){
        
        if(fields.containsKey(variablename)){
            return false;
        }
        
        FieldInfo newfield = new FieldInfo(type, variablename, fieldOffset);
        fields.put(variablename, newfield);
        fieldOffset += getSize(type);
        return true;
    }

    public boolean addMethod(String methodName, String returnType, LinkedList<String[]> parameters) {
       
        LinkedList<MethodInfo> methodList = methods.computeIfAbsent(methodName, k -> new LinkedList<>());
        LinkedList<String> parameterTypes = getTypes(parameters);
        
        for (MethodInfo m: methodList) {
            if (m.getParameterTypes().equals(parameterTypes)) {
                return false; // method with same name and parameter types already exists
            }
        }
        
        MethodInfo newMethod = new MethodInfo(methodName, returnType, methodOffset);
        methodList.add(newMethod);
        methodOffset += 8;
        return true;

    }

    public boolean addOverriddenMethod(String methodName, String returnType, LinkedList<String[]> parameters,int offset ) {
        
        
        return true;
    }

    
    public String getName(){
        return name;  
    }     
    public ClassInfo  getParentClass(){
        return parentClass ;
    }
    public Map<String,FieldInfo> getFields(){
        return fields;      
    }
    public Map<String,LinkedList<MethodInfo>>getMethods()  {
        return methods;    
    }    
    public int getFieldOffset(){
        return  fieldOffset;
    }
    public int getMethodOffset()  {
        return methodOffset;   
    }   

    //helper function 
    public int getSize(String type) {
        if (type.equals("int")) {
            return 4;
        } else if (type.equals("boolean")) {
            return 1;
        } else { // int[] and classes
            return 8;
        }
    }

    private LinkedList<String> getTypes(LinkedList<String[]> parameters) {
        
        LinkedList<String> parameterTypes = new LinkedList<>();
        for (String[] p: parameters) {
            parameterTypes.add(p[0]);
        }
        return parameterTypes;
    }
    

} 


