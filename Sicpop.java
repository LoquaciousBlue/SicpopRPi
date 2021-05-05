import java.util.LinkedList;
import java.util.Queue;

public class Sicpop {
    private SerialIOConnector sioConnector;
    private PrintJobHandler iHandler;
    private PrintingDispatcher pDispatcher;

    private Queue<Instructions> fileQueue;

    public Sicpop(){
        sioConnector = SerialIOConnector.getInstance();
        iHandler = PrintJobHandler.getInstance();
        pDispatcher = PrintingDispatcher.getInstance();
    }
}
