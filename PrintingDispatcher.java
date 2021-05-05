import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PrintingDispatcher {
    static private PrintingDispatcher instance;

    private BlockingQueue<Instructions> printQueue;

    private PrintingDispatcher(){
        printQueue = new LinkedBlockingQueue<>();
    }

    public static PrintingDispatcher getInstance(){
        if (instance == null) instance = new PrintingDispatcher();
        return instance;
    }

    public void addToPrintQueue(Instructions instr) throws InterruptedException {
        this.printQueue.put(instr);
    }
}
