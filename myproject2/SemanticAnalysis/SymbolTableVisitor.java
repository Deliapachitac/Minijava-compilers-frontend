package SemanticAnalysis;
import visitor.GJDepthFirst;
import syntaxtree.*;
import MySymbolTable.*;
import java.util.*;


public class SymbolTableVisitor extends  GJDepthFirst<String,AllClasses>{
    
    
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

        System.out.println("\n----------------------------------------");
        System.out.println("Processing main class " + classname);

        //
        if(!argu.addClass(classname, null)) {
            throw new Exception("Class already exists");
        }
           

        n.f14.accept(this,argu);
       
       
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

         //Class name is extracted 
        String classname= n.f1.accept(this, argu);
        System.out.println("\nClass   " + classname+"  {");
        
        //
        if(!argu.addClass(classname, null)) {
            throw new Exception("Class already exists");
        }

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
         //Class name is extracted 
        String classname= n.f1.accept(this, argu);
        String parentname= n.f3.accept(this, argu);

        System.out.println("\nClass  " + classname + "  extends " + parentname + " {" );
        
        //
        if(!argu.addClass(classname, parentname)) {
            throw new Exception("Class already exists");
        }

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
        String classname= n.f1.accept(this, argu);

        //if we are in a method we add local variable
        //otherwise we add field to the current class
        if(argu.getCurrentClass()!=null 
            && argu.getTypeVariable(classname)!=null
            && argu.isSubClass(argu.getCurrentClass(), argu.getCurrentClass())) {
                
                System.out.println("    Variable:  " + classname + " of type " + type );

                if(!argu.addLocalVar(classname, type)) { // the function returns false if the variable already exists
                   
                    throw new Exception("Variable already exists in local scope");
                }
        }else{
            System.out.println("    Field:  " + classname + " of type " + type );
            if (!argu.getCurrentClass().addField(type, classname)) {
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

        //Method name is extracted 
        String methodname= n.f2.accept(this, argu);
        String returnType= n.f1.accept(this, argu);
        
        //Collecting the parameters of the method in a linked list  
        LinkedList<String[]> myparametres= new LinkedList<>() ;
        if (n.f4.present()) {
            FormalParameterList paramList =(FormalParameterList) n.f4.node;
            
            myparametres.add(new String[]{paramList.f0.accept(this, argu), paramList.f1.accept(this, argu)});

            for(Node node:paramList.f1.f0.nodes) {
                FormalParameterTerm paramTerm = (FormalParameterTerm) node;
                myparametres.add(new String[]{paramTerm.f1.f0.accept(this, argu), paramTerm.f1.f1.accept(this, argu)});
            }
            
        }
        
        //
        LinkedList<String> paramTypes = new LinkedList<>();
        for(String[] p: myparametres) {
           
            paramTypes.add(p[0]);
        }

        MethodInfo parentmethod = argu.findMethod(argu.getCurrentClass().getParentClass(), methodname, paramTypes);
        int myoffset;
        if(parentmethod!=null) {
            myoffset = parentmethod.getOffset();
        }else {
            myoffset = -1;
        }

        System.out.println("    Method: " + returnType + " " + methodname  + "(" + paramTypes.toString() + ") ");
        //
        if (!argu.getCurrentClass().addMethod(methodname, returnType, myparametres, myoffset)) {
            throw new Exception("Method already exists in class");
        }

        argu.clearLocals();
        for(String[] p: myparametres) {
            if(!argu.addLocalVar(p[1], p[0])) { // the function returns false if the variable already exists
                throw new Exception("Variable already exists in local scope");
            }
        }

        n.f7.accept(this,argu);
        return null;
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

    @Override
    public String visit(Identifier n, AllClasses argu) {
        return n.f0.toString(); 
    }


}
