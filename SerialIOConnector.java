

public class SerialIOConnector {
    static private SerialIOConnector instance;

    private SerialIOConnector(){}

    public static SerialIOConnector getInstance(){
        if (instance == null) instance = new SerialIOConnector();
        return instance;
    }
}
