import java.io.IOException;
import java.io.File;

public class Main {
    //Sicpop printer = new Sicpop();
    public static void main(String[] args) throws IOException {
        Printer sicpop = new SicpopPrinter();
        File pdf = new File("printjob.pdf");
        Instructions printInstructions = Instructions.generateInstructions(pdf);
        sicpop.print(printInstructions);
    }
}