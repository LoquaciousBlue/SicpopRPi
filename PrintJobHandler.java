public class PrintJobHandler {
    static private PrintJobHandler instance;

    private PrintJobHandler(){}

    public static PrintJobHandler getInstance(){
        if (instance == null) instance = new PrintJobHandler();
        return instance;
    }
}
