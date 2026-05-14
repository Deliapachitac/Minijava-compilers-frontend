package SemanticAnalysis;
import visitor.GJDepthFirst;
import syntaxtree.*;
import MySymbolTable.*;


public class SymbolTableVisitor extends  GJDepthFirst<String,AllClasses>{
    
    AllClasses all_classes;
    ClassInfo current_class;
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
    public String visit(MainClass n, AllClasses argu) throws Exception{

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
    public String visit(ClassDeclaration n, AllClasses argu) throws Exception{

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
    public String visit(ClassExtendsDeclaration n, AllClasses argu) throws Exception{ 
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
    public String visit(VarDeclaration n, AllClasses argu) throws Exception{
        
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
    public String visit(MethodDeclaration n, AllClasses argu) throws Exception{

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
    public String  visit(FormalParameterList n,AllClasses argu) throws  Exception {     
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return null;
    }
    /**
    * f0 -> Type()
    * f1 -> Identifier()
    */

    @Override
    public String visit(FormalParameter n, AllClasses argu) throws Exception{
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
    public String visit(FormalParameterTerm n,AllClasses  argu)throws  Exception{
        return n.f1.accept(this, argu);

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
