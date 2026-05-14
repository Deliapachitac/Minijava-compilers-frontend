package MySymbolTable;
import java.util.*;


public class AllClasses {
    
    private LinkedHashMap<String, ClassInfo> classes;

    // This are helpful variables that are set by visitors to keep track od the current position on the AST  
    private ClassInfo currentClass;
    private LinkedHashMap<String, String>localvar ;

    public AllClasses() {
        this.classes = new LinkedHashMap<>();
        this.localvar = new LinkedHashMap<>();

    }

    public boolean addClass(String name, String parentName) {
        if (classes.containsKey(name)) {
            return false; // class with the same name already exists
        }

        ClassInfo parentClass = null;
        if (parentName != null) {
            parentClass = classes.get(parentName);
            if (parentClass == null) {
                return false; // parent class does not exist
            }
        }

        ClassInfo newClass = new ClassInfo(name, parentClass);
        classes.put(name, newClass);
        currentClass = newClass; // set the current class to the newly added class
        return true;

    }


    public void setCurrentClass(String name) {
        currentClass = classes.get(name);
    }

    public ClassInfo getCurrentClass() {
        return currentClass;
    }
    public LinkedHashMap<String, ClassInfo> getClasses() {
        return classes;
    }

    public boolean addLocalVar(String name, String type) {
        if (localvar.containsKey(name)) {
            return false; 
        }
        localvar.put(name, type);
        return true;
    }

    //true=> if myclass is a subclass of parentclass
    //false=> if myclass is not a subclass of parentclass
    public boolean isSubClass(ClassInfo myclass, ClassInfo parentclass) {
        if(myclass.equals(parentclass)){
            return true;
        }          

        ClassInfo current = myclass.getParentClass();
        while (current!=null) {
            if ( current.equals(parentclass)){
                return  true;            
            }        
            current =current.getParentClass();
        }
        return  false;
    }
        

    // Returns the type of the variable with the given name, or null if it does not exist
    public String  getTypeVariable(String name) {
        
        if (localvar.containsKey(name)) {
            return localvar.get(name); 
        }

        ClassInfo current = currentClass;
        while (current != null) {
            if (current.getFields().containsKey(name)) {
                return current.getFields().get(name).getType();
            }
            current = current.getParentClass();
        }

        return null;
    }

    // Finds if there is a method with the given name and parameter types in the current class or any of its parent classes
    // if found => returns MethodInfo of the method
    // if not found => returns null
    public MethodInfo findMethod(ClassInfo myclass,String name, LinkedList<String> parameterTypes) {
        ClassInfo current = currentClass;
        while (current != null) {
            LinkedList<MethodInfo> methodList = current.getMethods().get(name);
            if (methodList != null) {
                for (MethodInfo m : methodList) {
                    if (m.getParameterTypes().equals(parameterTypes)) {
                        return m;
                    }
                }
            }
            current = current.getParentClass();
        }
        return null; 
    }

    
    public void clearLocals(){
        this.localvar.clear();
    }

}
