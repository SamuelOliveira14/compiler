package main;
import syntactic.SyntacticAnalysis;
public class Main {
    public static void main(String[] args) {

        if (args.length < 1){
            System.out.println("Usage: java Main [source code path]");
            System.exit(1);
        }

        String path = args[0];
        
        SyntacticAnalysis parser = new SyntacticAnalysis(path);

        parser.start();

    }
}
