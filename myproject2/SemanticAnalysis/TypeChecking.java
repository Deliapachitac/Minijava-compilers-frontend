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
            for(syntaxtree.Node node :paramList.f1.f0.nodes) {
                FormalParameterTerm paramTerm = (FormalParameterTerm) node;
                
                mytype= paramTerm.f1.f0.accept(this, argu);
                myname= paramTerm.f1.f1.accept(this, argu);
                System.out.println("    Parameter: " + mytype + " " + myname);
                argu.addLocalVar(myname, mytype); // add the parameter to the local variables of the method
            }



        }

        // n.f7.accept(this, argu); // var declarations of the method
        
        if(n.f7.present()) {
            for(syntaxtree.Node node : n.f7.nodes) {
                VarDeclaration varDecl =(VarDeclaration) node;
                String var_type= varDecl.f0.accept(this, argu);// gets ex "int", "boolean", "Element", etc.
                String var_name= varDecl.f1.accept(this, argu);// gets the name of the variable
                argu.addLocalVar(var_name, var_type); // add the local variable to the local variables of the method
            }
        }
        
        n.f8.accept(this, argu); // this will visit the statements of the method (if,while,print,...)

        //Checking the return type of the method
        String return_expr_type= n.f10.accept(this, argu);
        String type_return_expr="";

        if(return_expr_type !=null ){
            String temp= argu.getTypeVariable(return_expr_type);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_return_expr= temp;
            }else{
                //if the variable wasnt found then the return_expr_type is already the type 
                type_return_expr=return_expr_type;
            }
        }

        
        // we check if the parameter types match
        boolean match= type_return_expr.equals(returnType);
        if(!match) {
            ClassInfo expected_class= argu.getClassInfoByName(type_return_expr);
            ClassInfo given_class= argu.getClassInfoByName(returnType);
            if( argu.isSubClass(given_class, expected_class)&&expected_class!=null && given_class!=null ) {
                match =true;
            } 
        }


        if(!match) {
            throw new Exception("The type of the return " + type_return_expr + " is not compatible with the declared return type  " + returnType);
        }


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
    @Override  
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
   @Override  
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
   @Override  
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
   @Override  
   public String visit(AndExpression n, AllClasses argu) throws Exception {
        String left= n.f0.accept(this, argu);
        String right= n.f2.accept(this, argu);

        String type_left="";
        String type_right="";


        if(left !=null ){
            String temp= argu.getTypeVariable(left);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_left= temp;
            }else{
                //if the variable wasnt found then the left is already the type 
                type_left=left;
            }
        }

        if(right !=null ){
            String temp= argu.getTypeVariable(right);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_right= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_right=right;
            }
        }


        if(!type_right.equals("boolean") || !type_left.equals("boolean") ) {
            throw new Exception("Both operands of && must be of type boolean");
        }

        return "boolean";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   @Override  
   public String visit(CompareExpression n, AllClasses argu) throws Exception {
        String left= n.f0.accept(this, argu);
        String right= n.f2.accept(this, argu);

        String type_left="";
        String type_right="";


        if(left !=null ){
            String temp= argu.getTypeVariable(left);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_left= temp;
            }else{
                //if the variable wasnt found then the left is already the type 
                type_left=left;
            }
        }

        if(right !=null ){
            String temp= argu.getTypeVariable(right);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_right= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_right=right;
            }
        }


        if(!type_right.equals("int") || !type_left.equals("int") ) {
            throw new Exception("Both operands of < must be of type int");
        }

        return "boolean";// the result of a comparison is always boolean
     
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   @Override  
   public String visit(PlusExpression n, AllClasses argu) throws Exception {
        String left= n.f0.accept(this, argu);
        String right= n.f2.accept(this, argu);

        String type_left="";
        String type_right="";


        if(left !=null ){
            String temp= argu.getTypeVariable(left);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_left= temp;
            }else{
                //if the variable wasnt found then the left is already the type 
                type_left=left;
            }
        }

        if(right !=null ){
            String temp= argu.getTypeVariable(right);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_right= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_right=right;
            }
        }


        if(!type_right.equals("int") || !type_left.equals("int") ) {
            throw new Exception("Both operands of + must be of type int");
        }

        return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   @Override  
   public String visit(MinusExpression n, AllClasses argu) throws Exception {
        String left= n.f0.accept(this, argu);
        String right= n.f2.accept(this ,argu);

        String type_left="";   
        String type_right="";


        if(left !=null ){
            String temp= argu.getTypeVariable(left);
            if(temp!= null){
                // if the variable was found then return their type (ex. Element->int)
                type_left= temp;
            }else{
                //if the variable wasnt found then the left is already the type 
                type_left=left;
            }
        }

        if(right !=null ){
            String temp= argu.getTypeVariable(right);
            if(temp !=null){
                // if the variable was found then return their type (ex. Element->int)
                type_right= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_right=right;
            }
        }


        if(!type_right.equals("int")|| !type_left.equals("int") ) {
            throw new Exception("Both operands of - must be of type int");
        }

        return "int";
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   @Override  
   public String visit(TimesExpression n, AllClasses argu) throws Exception {
        String left= n.f0.accept(this, argu);
        String right= n.f2.accept(this, argu);

        String type_left="";
        String type_right="";


        if(left !=null ){
            String temp= argu.getTypeVariable(left);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_left= temp;
            }else{
                //if the variable wasnt found then the left is already the type 
                type_left=left;
            }
        }

        if(right !=null ){
            String temp= argu.getTypeVariable(right);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_right= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_right=right;
            }
        }


        if(!type_right.equals("int") || !type_left.equals("int") ) {
            throw new Exception("Both operands of + must be of type int");
        }

        return "int";
   }

    /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   @Override  
   public String visit(NotExpression n, AllClasses argu) throws Exception {
       
        String right= n.f1.accept(this, argu);
        String type_right="";

        if(right !=null ){
            String temp= argu.getTypeVariable(right);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_right= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_right=right;
            }
        }


        if(!type_right.equals("boolean")  ) {
            throw new Exception("Both operands of ! must be of type boolean");
        }

        return "boolean";
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   @Override  
   public String visit(BracketExpression n, AllClasses argu) throws Exception {
        return n.f1.accept(this, argu);
   }


   //******************* Array operations checking *****************//

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   @Override  
   public String visit(ArrayLength n, AllClasses argu) throws Exception {
        String expr= n.f0.accept(this, argu);
        String type_expr="" ;

        if(expr !=null ){
            String temp= argu.getTypeVariable(expr);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_expr= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_expr=expr;
            }
        }


        if(!type_expr.equals("int[]")  ) {
            throw new Exception("The .lenght operator can only be applied to int[]");
        }

        return "int"; // the result of .length is always an integer
   }
   
   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   @Override  
   public String visit(ArrayAllocationExpression n, AllClasses argu) throws Exception {
        String expr= n.f3.accept(this, argu);
        String type_expr="";

        if(expr !=null ){
            String temp= argu.getTypeVariable(expr);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_expr= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_expr=expr;
            }
        }


        if(!type_expr.equals("int")  ) {
            throw new Exception("Array allocation must be have  integer size");
        }

        return "int[]"; //we create an array of integers 
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   @Override  
   public String visit(ArrayLookup n, AllClasses argu) throws Exception {
        String myarray= n.f0.accept(this, argu);
        String myindex= n.f2.accept(this, argu);

        String type_array="";
        String type_index="";


        if(myarray !=null ){
            String temp= argu.getTypeVariable(myarray);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_array= temp;
            }else{
                //if the variable wasnt found then the left is already the type 
                type_array=myarray;
            }
        }

        if(myindex !=null ){
            String temp= argu.getTypeVariable(myindex);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_index= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_index=myindex;
            }
        }


        if(!type_index.equals("int") || !type_array.equals("int[]") ) {
            throw new Exception("TThe index of an array must be of type int and the array must be of type int[]");
        }

        return "int"; // the result of looking up an array is always an integer
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
   @Override  
   public String visit(ArrayAssignmentStatement n, AllClasses argu) throws Exception {
        
        String myarray= n.f0.accept(this, argu);
        String myindex= n.f2.accept(this, argu);
        String myvalue = n.f5.accept(this, argu);


        String type_array="";
        String type_index="";
        String type_value="";

        if(myarray !=null ){
            String temp= argu.getTypeVariable(myarray);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_array= temp;
            }else{
                //if the variable wasnt found then the left is already the type 
                type_array=myarray;
            }
        }

        if(myindex !=null ){
            String temp= argu.getTypeVariable(myindex);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_index= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_index=myindex;
            }
        }

        if(myvalue !=null ){
            String temp= argu.getTypeVariable(myvalue);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_value= temp;
            } else{
                //if the variable wasnt found then the right is already the type 
                type_value=myvalue;
            }
        }

        if(!type_index.equals("int") || !type_array.equals("int[]")) {
            throw new Exception("TThe index of an array must be of type int and the array must be of type int[]");
        }

        if(!type_value.equals("int")) {
            throw new Exception("The value assigned to an array element must be of type int");
        }

        return null; // array assignment doesnt produce a value
   }




   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   @Override  
   public String visit(AllocationExpression n, AllClasses argu) throws Exception {
        //Class name is extracted 
        String classname= n.f1.accept(this, argu);

        //////////////// ylopoihse to 
        if(argu.getClassInfoByName(classname) == null) {
            throw new Exception("Class " + classname + " not found");
        }
        
      return classname;// returning the name if the class as a type
   }


   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   @Override  
   public String visit(AssignmentStatement n, AllClasses argu) throws Exception {
        String ident_name= n.f0.accept(this, argu);
        String expr= n.f2.accept(this ,argu);

        String type_ident="";   
        String type_expr="";


        if(ident_name !=null ){
            String temp= argu.getTypeVariable(ident_name);
            type_ident=temp;
            
        }

        if(type_ident==null) {
            throw new Exception("Variable " + ident_name + " not found");
        }

        if(expr !=null ){
            String temp= argu.getTypeVariable(expr);
            if(temp !=null){
                // if the variable was found then return their type (ex. Element->int)
                type_expr= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_expr=expr;
            }
        }

        if(!type_expr.equals(type_ident)) {
            throw new Exception("The type of the expression assigned to " + ident_name + " doesnt match with the declared type " + type_ident);
        }


        return null;
   }


   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   @Override  
   public String visit(MessageSend n, AllClasses argu) throws Exception {
        String object= n.f0.accept(this, argu);
        String type_object="";

        if(object !=null ){
            String temp= argu.getTypeVariable(object);
            if(temp!=null){
                // if the variable was found then return their type (ex. Element->int)
                type_object= temp;
            }else{
                //if the variable wasnt found then the right is already the type 
                type_object=object;
            }
        }

        ClassInfo class_info= argu.getClassInfoByName(type_object);
        if(class_info==null) {
            throw new Exception("The class  " + object + "was  not found");
        }      

        String method_name= n.f2.accept(this, argu);

        // We extract the types of the arguments of the method call
        LinkedList<String> list_arg_types= new LinkedList<>();
        if(n.f4.present()) {
            ExpressionList exprList =  (ExpressionList) n.f4.node;

            String arg= exprList.f0.accept(this, argu);
            String arg_type="";
            if(arg !=null ){
                String temp= argu.getTypeVariable(arg);
                if(temp!=null){
                    // if the variable was found then return their type (ex. Element->int)
                    arg_type= temp;
                }else{
                    //if the variable wasnt found then the right is already the type 
                    arg_type=arg;
                }
            }
            list_arg_types.add(arg_type);
            for(syntaxtree.Node node : exprList.f1.f0.nodes) {

                ExpressionTerm exprTerm= (ExpressionTerm) node;
                arg= exprTerm.f1.accept(this, argu);
                arg_type="";
                if(arg !=null ){
                    String temp= argu.getTypeVariable(arg);
                    if(temp!=null){
                        // if the variable was found then return their type (ex. Element->int)
                        arg_type= temp;
                    }else{
                        //if the variable wasnt found then the right is already the type    
                        arg_type=arg;   
                    }
                }
                list_arg_types.add(arg_type);
            }
        
        
        }

        MethodInfo method_info= argu.findMethod(class_info, method_name, list_arg_types);
        System.out.println(" " + method_info);
        System.out.println("Type checking method call " + method_name + " on object of type " + type_object + " with argument types " + list_arg_types.toString());
        if(method_info==null) {
            throw new Exception("Method " + method_name + " with the given argument types was not found in class " + type_object + " or its parent classes");
        }

        return method_info.getReturnType(); // the type of a method call is the return type of the method

    }
        


}
   
