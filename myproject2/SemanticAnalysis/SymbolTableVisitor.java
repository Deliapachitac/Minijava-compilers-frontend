package SemanticAnalysis;

import syntaxtree.*;
import MySymbolTable.*;
import java.util.*;
import visitor.GJDepthFirst;

public class SymbolTableVisitor extends  GJDepthFirst<String,AllClasses>{
    
    private boolean inMethod = false; //this is flag to check if we are currently visiting  a method or not  (ΩαρΔεψλαρατιον is λοψαλ variable or field to the class )
   
    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
    
    @Override
    public String visit(MainClass n, AllClasses argu) throws Exception{

        //Class name is extracted 
        String classname= n.f1.accept(this, argu);

        
        System.out.println("Processing main class " + classname);

        //Adding the main class to the symbol table with no parent class
        if(!argu.addClass(classname, null)) {
            throw new Exception("Class already exists");
        }
        

        inMethod = true; // we are now visiting the main method
        argu.clearLocals(); // clear any local variables from previous classes 
        
        // visit and register the local variables of the main method
        n.f14.accept(this,argu);

        //exiting the main method
        inMethod = false;
       
        return null;
    }

     /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */

    @Override
    public String visit(ClassDeclaration n, AllClasses argu) throws Exception{

         //Class name is extracted and printed
        String classname= n.f1.accept(this, argu);
        System.out.println("\nClass   " + classname+"  {");
        
        //Adding the class to the symbol table with no parent class
        if(!argu.addClass(classname, null)) {
            throw new Exception("Class already exists");
        }

        //w e are visiting the class not method 
        inMethod = false;

        n.f3.accept(this, argu);// fields
        n.f4.accept(this, argu);// methods
           
        System.out.println("}");
        return null;

    }



    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   
    @Override
    public String visit(ClassExtendsDeclaration n, AllClasses argu) throws Exception{ 
        
        //Class name and parent name are extracted and printed
        String classname= n.f1.accept(this, argu);
        String parentname= n.f3.accept(this, argu);

        System.out.println("\nClass  " + classname + "  extends " + parentname + " {" );
        
        //Adding the class to the symbol table with parent class 
        if(!argu.addClass(classname, parentname)) {
            throw new Exception("Class already exists");
        }

        //we are visiting the class not method
        inMethod = false;
        n.f5.accept(this, argu);// fields
        n.f6.accept(this, argu);// methods


        System.out.println("}");
        return null;

    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */

    @Override
    public String visit(VarDeclaration n, AllClasses argu) throws Exception{
        
        //Class name is extracted 
        String type= n.f0.accept(this, argu);
        String field_variable_name= n.f1.accept(this, argu);

        //if we are in a method we add local variable
        //otherwise we add field to the current class
        if(inMethod) {
            
            System.out.println("       Variable:  " + type + " " + field_variable_name);

            // we are adding a local variable to the current method
            if(!argu.addLocalVar(field_variable_name, type)) { 
                
                throw new Exception("Variable already exists in local scope");
            }
        }else{
            System.out.println("    Field:  " + type + " " + field_variable_name );
            
            // we are adding a field to the current class 
            if (!argu.getCurrentClass().addField(type, field_variable_name)) {
                throw new Exception("Field already exists in class");
            }
        }
        return null;
    }

    /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */

    @Override
    public String visit(MethodDeclaration n, AllClasses argu) throws Exception{

        //Method name and return type are extracted  
        String returnType= n.f1.accept(this, argu);
        String methodname= n.f2.accept(this, argu);
        
        
        //Collecting the parameters of the method in a linked list  
        LinkedList<String[]> myparametres= new LinkedList<>() ;
        if (n.f4.present()) {

            //We save the parametres in a temporary FormalParameterList(is a class that is produced by the JTB parser))
            FormalParameterList paramList =(FormalParameterList) n.f4.node;
            
            //We extract the first parameter of the method and save it in the list of parameters of the method
            String mytype= paramList.f0.f0.accept(this, argu);
            String myname= paramList.f0.f1.accept(this, argu);
            String[] myparam =new String[]{mytype, myname}; 
            myparametres.add(myparam);

            // For each
            for(Node node :paramList.f1.f0.nodes) {
                FormalParameterTerm paramTerm = (FormalParameterTerm) node;
                
                mytype= paramTerm.f1.f0.accept(this, argu);
                myname= paramTerm.f1.f1.accept(this, argu);
                myparam =new String[]{mytype, myname}; 
                myparametres.add(myparam);
            }
            
        }
        
        //Save only the types of the parametres for the method signature check
        LinkedList<String> paramTypes = new LinkedList<>();
        for(String[] p: myparametres) {
           
            paramTypes.add(p[0]);
        }

        /////////////////////////???? not sure yet 
        MethodInfo parentmethod = argu.findMethod(argu.getCurrentClass().getParentClass(), methodname, paramTypes);
        int myoffset;
        if(parentmethod!=null) {
            myoffset = parentmethod.getOffset();
        }else {
            myoffset = -1;
        }

        System.out.println("    Method: " + returnType + " " + methodname  + "(" + paramTypes.toString() + ") ");
        
        //adding the method to the current class
        if (!argu.getCurrentClass().addMethod(methodname, returnType, myparametres, myoffset)) {
            throw new Exception("Method already exists in class");
        }

        // we are now visiting the method 
        inMethod= true;
        argu.clearLocals();// clear any local variables from previous methods

        for(String[] p: myparametres) {
            if(!argu.addLocalVar(p[1], p[0])) { // the function returns false if the variable already exists
                throw new Exception("Variable already exists in local scope");
            }
        }

        n.f7.accept(this,argu);

        inMethod = false;
        return null;
    }

   // for identifiers we return the name of the identifier(class name, variable name, method name, etc.)
    @Override
    public String visit(Identifier n, AllClasses argu) {
        return n.f0.toString(); 
    }
  

    //types
    @Override
    public String visit(IntegerType n, AllClasses argu) {
        return  "int";     
    }

    @Override
    public String visit(BooleanType n, AllClasses argu) {
        return "boolean" ;
    }

    @Override
    public String visit(ArrayType n, AllClasses argu) {
        return "int[]";
    }

   

}
