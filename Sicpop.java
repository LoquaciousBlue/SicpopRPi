import java.io.File;
import java.io.IOException;

public class Sicpop {
    private Printer service;

    public Sicpop(){
        service = new SicpopPrinter();
    }

    public void performPrintJob() throws IOException {
        File fileToPrint = new File("print.pdf");
        Instructions instr = Instructions.generateInstructions(fileToPrint);
        this.service.print(instr);
    }
}
