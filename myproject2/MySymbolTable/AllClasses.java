package MySymbolTable;
import java.util.LinkedHashMap;


public class AllClasses {
    
    private LinkedHashMap<String, ClassInfo> classes;

    // This are helpful variables that are set by visitors to keep track od the current position on the AST  
    private ClassInfo currentClass;
    private String currentMethod;
    private LinkedHashMap<String, String>localvar ;

    public AllClasses() {
        this.classes = new LinkedHashMap<String, ClassInfo>();
        this.localvar = new LinkedHashMap<String, String>();

    }




}
