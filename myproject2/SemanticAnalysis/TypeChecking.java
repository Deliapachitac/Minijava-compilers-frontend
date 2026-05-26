package SemanticAnalysis;

import syntaxtree.*;
import MySymbolTable.*;
import java.util.*;
import visitor.GJDepthFirst;
    


public class TypeChecking extends  GJDepthFirst<String,AllClasses>{
    
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

        System.out.println("Type checking main class " + classname);
        
        argu.setCurrentClass(classname); // set the current class to the class we are visiting  
        argu.clearLocals(); // clear any local variables from previous classes 
        
        // visit var declarations of the main method
        n.f14.accept(this,argu);

        // visit statements of the main method
        n.f15.accept(this,argu);
       
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

        System.out.println("Type checking class " + classname);
        
        argu.setCurrentClass(classname); // set the current class to the class we are visiting  
        argu.clearLocals(); // clear any local variables from previous classes 
        
        // visit var declarations of the class
        n.f3.accept(this,argu);
        // visit method declarations of the class
        n.f4.accept(this,argu);

       
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

        System.out.println("Type checking class " + classname +"extends " + parentname);

        argu.setCurrentClass(classname); // set the current class to the class we are visiting  
        argu.clearLocals(); // clear any local variables from previous classes 
        
        // visit var declarations of the class
        n.f5.accept(this,argu);
        // visit method declarations of the subclass
        n.f6.accept(this,argu);

        return null;
    }


    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */

    @Override
    public String visit(VarDeclaration n, AllClasses argu) throws Exception{
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

        System.out.println("Type checking method " + methodname + " with return type " + returnType);
        argu.clearLocals(); // clear any local variables from previous methods
        
        
        if(n.f4.present()) {
            
            FormalParameterList paramList =(FormalParameterList) n.f4.node;
            
            //We extract the first parameter of the method and save it in the list of parameters of the method
            String mytype= paramList.f0.f0.accept(this, argu);
            String myname= paramList.f0.f1.accept(this, argu);
            System.out.println("    Parameter: " + mytype + " " + myname);
            argu.addLocalVar(myname, mytype); // add the first parameter to the local variables of the method

            // For each
            for(Node node :paramList.f1.f0.nodes) {
                FormalParameterTerm paramTerm = (FormalParameterTerm) node;
                
                mytype= paramTerm.f1.f0.accept(this, argu);
                myname= paramTerm.f1.f1.accept(this, argu);
                System.out.println("    Parameter: " + mytype + " " + myname);
                argu.addLocalVar(myname, mytype); // add the parameter to the local variables of the method
            }



        }

        n.f7.accept(this, argu); // var declarations of the method
        n.f8.accept(this, argu); // this will visit the statements of the method (if,while,print,...)

        /////////


        return null;
    }
    
    
    //****************Control flow statements***********************//
    
    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */

   public String visit(WhileStatement n, AllClasses argu) throws Exception {
       //We take the result of f2 (ex: "Element", "int")
        String raw_type = n.f2.accept(this, argu);
        String type_condition="";

        if(raw_type !=null ){
            String temp= argu.getTypeVariable(raw_type);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_condition= temp;
            }else{
                //if the variable wasnt found then the raw_type is already the type 
                type_condition=raw_type;
            }
        }

        System.out.println("Type checking while statement with condition type: " + type_condition);
        if(!type_condition.equals("boolean")) {
            throw new Exception("Condition  of while statement must be of type boolean");
        }

        n.f4.accept(this, argu); // visit the body of the while statement
        return null;
   }

      /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public String visit(IfStatement n, AllClasses argu) throws Exception {
        //We take the result of f2 (ex: "Element", "int")
        String raw_type = n.f2.accept(this, argu);
        String type_condition="";

      
        if(raw_type !=null ){
            String temp= argu.getTypeVariable(raw_type);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_condition= temp;
            }else{
                //if the variable wasnt found then the raw_type is already the type 
                type_condition=raw_type;
            }
        }

        System.out.println("Type checking if statement with condition type: " + type_condition);
        if(!type_condition.equals("boolean")) {
            throw new Exception("Condition  of if statement must be of type boolean");
        }

        n.f4.accept(this, argu); // visit the body of the if statement
        n.f6.accept(this, argu); // visit the body of the else statement
        return null;
   }


    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public String visit(PrintStatement n, AllClasses argu) throws Exception {
        
        //We take the result of f2 (ex: "Element", "int")
        String raw_type = n.f2.accept(this, argu);
        String type_condition="";

        if(raw_type !=null ){
            String temp= argu.getTypeVariable(raw_type);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_condition= temp;
            }else{
                //if the variable wasnt found then the raw_type is already the type 
                type_condition=raw_type;
            }
        }

        System.out.println("Type checking print statement with expression type: " + type_condition);
        if(!type_condition.equals("int")) {
            throw new Exception("Println statement only prints integers");
        }

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


    @Override
    public String visit(IntegerLiteral n, AllClasses argu){
        return "int";
    }
    @Override
    public String visit(TrueLiteral n ,AllClasses argu){
        return "boolean";
    }
    @Override
    public String visit(FalseLiteral n ,AllClasses argu){
        return "boolean";
    }
    @Override
    public String visit(ThisExpression n ,AllClasses argu){
        return argu.getCurrentClass().getName();   
    }

    //********************Arithmetic and Logical Expressions*****************//

    /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
   public String visit(AndExpression n, AllClasses argu) throws Exception {
      
        return null ;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public String visit(CompareExpression n, AllClasses argu) throws Exception {
     
      return null;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public String visit(PlusExpression n, AllClasses argu) throws Exception {
      
      return null;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public String visit(MinusExpression n, AllClasses argu) throws Exception {
      
      return null;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public String visit(TimesExpression n, AllClasses argu) throws Exception {
      
      return null;
   }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   public String visit(NotExpression n, AllClasses argu) throws Exception {
     
      return null;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public String visit(BracketExpression n, AllClasses argu) throws Exception {
      return null;
   }


   //******************* Array operations checking *****************//

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public String visit(ArrayLength n, AllClasses argu) throws Exception {
      return null;
   }
   
   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public String visit(ArrayAllocationExpression n, AllClasses argu) throws Exception {
      
      return null;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public String visit(ArrayLookup n, AllClasses argu) throws Exception {
      
      return null;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public String visit(ArrayAssignmentStatement n, AllClasses argu) throws Exception {
      
      return null;
   }

}
   
