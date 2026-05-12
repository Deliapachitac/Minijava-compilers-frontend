package SemanticAnalysis;
import visitor.GJDepthFirst;
import syntaxtree.*;
import MySymbolTable.*;

import java.lang.reflect.Method;
import java.util.LinkedList;



public class SymbolTableVisitor extends  GJDepthFirst<String,Void>{
    
    AllClasses all_classes;
    Info current_class;
    MethodInfo current_method;

    public SymbolTableVisitor() {}
    
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
    public String visit(MainClass n, Void argu) throws Exception{

        //Class name is extracted 
        String classname= n.f1.accept(this, argu);

        //
        try{
            all_classes.addClass(classname,false , null  );
            current_class = all_classes.findClass(classname);
            n.f11.accept(this, argu);
            n.f14.accept(this, argu);
            n.f15.accept(this, argu);
            
        }catch(Exception e){
            System.err.println(e.getMessage());
            throw e;
        }
        current_class=null;
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
    public String visit(ClassDeclaration n, Void argu) throws Exception{

         //Class name is extracted 
        String classname= n.f1.accept(this, argu);

        //
        try{
            all_classes.addClass(classname,false , null  );
            current_class = all_classes.findClass(classname);
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            
        }catch(Exception e){
            System.err.println(e.getMessage());
            throw e;
        }
        current_class=null;
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
    public String visit(ClassExtendsDeclaration n, Void argu) throws Exception{ 
         //Class name is extracted 
        String classname= n.f1.accept(this, argu);
        String inheritedname= n.f3.accept(this, argu);
        //
        try{
            if(all_classes.findClass(inheritedname) == null){
                throw new Exception("Inherited class does not exist" );
            }
            all_classes.addClass(classname,true , inheritedname  );
            current_class = all_classes.findClass(classname);
            n.f3.accept(this, argu);
            n.f4.accept(this, argu);
            
        }catch(Exception e){
            System.err.println(e.getMessage());
            throw e;
        }
        current_class=null;
        return null;

    }

    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */

    @Override
    public String visit(VarDeclaration n, Void argu) throws Exception{
        
        //Class name is extracted 
        String type= n.f0.accept(this, argu);
        String classname= n.f1.accept(this, argu);

        //
        try{
            if(current_method!=null){
                current_method.addLocalVariable(type, classname);
            }
            else  {
                current_class.addVariable(type, classname);
            }
            
        }catch(Exception e){
            System.err.println(e.getMessage());
            throw e;
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
    public String visit(MethodDeclaration n, Void argu) throws Exception{

        //Method name is extracted 
        String methodname= n.f2.accept(this, argu);
        String returnType= n.f1.accept(this, argu);
        current_method= new MethodInfo(methodname, returnType);
    
        try{
            n.f4.accept(this, argu);
            current_class.addMethod(methodname, returnType, current_method.parameters);
            n.f7.accept(this, argu);
            n.f8.accept(this, argu);
            
        }catch(Exception e){
            System.err.println(e.getMessage());
            throw e;
        }
        current_method= null;
        return null;
    }

    /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */

    @Override
    public String  visit(FormalParameterList n,Void argu) throws  Exception {     
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return null;
    }
    /**
    * f0 -> Type()
    * f1 -> Identifier()
    */

    @Override
    public String visit(FormalParameter n, Void argu) throws Exception{
        String type = n.f0.accept(this, argu);
        String parameterName = n.f1.accept(this, argu);
        try{
            current_method.addParameter(type, parameterName);
        }catch(Exception e){
            System.err.println(e.getMessage());
            throw e;    
        }
        return null;
    }

    /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */

    @Override
    public String visit(FormalParameterTerm n,Void  argu)throws  Exception{
        return n.f1.accept(this, argu);

    }

    //types
    @Override
    public String visit(IntegerType n, Void argu) {
        return  "int";     
    }

    @Override
    public String visit(BooleanType n, Void argu) {
        return "boolean" ;
    }

    @Override
    public String visit(ArrayType n, Void argu) {
        return "int[]";
    }

    @Override
    public String visit(Identifier n, Void argu) {
        return n.f0.toString(); 
    }


}
