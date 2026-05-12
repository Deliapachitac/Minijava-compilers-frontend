package MySymbolTable;
import java.util.LinkedList;

public class AllClasses {
    LinkedList<Info> classes;

    public AllClasses() {
        classes = new LinkedList<Info>();
    }

    public void addClass(String name, boolean inherited, String inherited_name)   throws Exception  {
        
        // Check if the class already exists
        if(findClass(name)!= null){
            throw new Exception("Class already exists") ;      
        }

        Info new_class =new Info(name, inherited, inherited_name);
        classes.add(new_class);
    }   
           
    public Info findClass(String name){          
        for (Info current:classes) {
            if (current.name.equals( name )){
                return  current; 
            }   
        }    
        return  null;
    }

    

}
