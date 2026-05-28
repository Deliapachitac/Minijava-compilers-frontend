import java.util.LinkedList;
import java.io.* ;
import syntaxtree.*;
import SemanticAnalysis.*;
import MySymbolTable.* ;


public class main {
    public static void main(String[] args) throws Exception {
       
        // Check if the user provided at least one file as an argument
        if (args.length <1){
            System.err.println("Usage: java main [file1] [file2] ... [fileN]");
            System.exit(1);
        }

        for (String fileName : args) { 
            
            System.out.println ("Processing file " + fileName);
            
            
            try (FileInputStream fin = new FileInputStream(fileName);){
               
                // Create a parser for the input file and parse it to get the root of the syntax tree
                MiniJavaParser myparser =new  MiniJavaParser(fin);
                Goal root= myparser.Goal();

                //build the new symbol table
                AllClasses all_classes = new AllClasses();
                
                SymbolTableVisitor root_visitor = new SymbolTableVisitor();
                root.accept(root_visitor, all_classes);

                all_classes.printOffset();

                //type checking
                TypeChecking type_checking_visitor = new TypeChecking();
                root.accept(type_checking_visitor, all_classes);    

                

            } catch (Exception e) {
                System.err.println("Semantic analysis failed for " + fileName);
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }

        System.out.println("All files processed  successfully " );

    }
}
