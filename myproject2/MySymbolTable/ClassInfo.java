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

    // The offset meaning:
    // if offset is -1 it means that the method is new or overloaded
    // if offest>=0 it means that the method is an override and it has the same offset
    
    //Overloading rules :
    // - Same name , different number of args => valid
    // - Same name, same number of args and same types => NOT valid
    // - Same name , same number of args and subtype relation(ex. Cat extends Animal) => NOT valid
    // - Same name, same number of args and at least one position with no subtype relation => valid
    public boolean addMethod(String methodName, String returnType, LinkedList<String[]> parameters,int offest){
       
        LinkedList<MethodInfo> methodList = methods.computeIfAbsent(methodName, k -> new LinkedList<>());
        LinkedList<String> parameterTypes = getTypes(parameters);
        
        for (MethodInfo m: methodList) {
            LinkedList<String> existingParamTypes = m.getParameterTypes();
            if (existingParamTypes.size() == parameterTypes.size()) {
               
                boolean match=true;

                for (int i = 0; i <parameterTypes.size(); i++) {
                    String  type1= existingParamTypes.get(i);
                    String  type2= parameterTypes.get(i);

                    if(!type1.equals(type2)) {
                        match=false;
                    }

                }
                if(match) {
                    return false; // method with same name and parameter types already exists
                }


            }
        }

        int myoffset;
        if(offest==-1){//new method or overloaded
            myoffset = methodOffset;
            methodOffset += 8;

        }else{//override
            myoffset = offest;
        }
        
        MethodInfo newMethod = new MethodInfo(methodName, returnType, myoffset);
        for (String[] p: parameters) {
            newMethod.addParameter(p[0], p[1]);
        }
        methodList.add(newMethod);
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


