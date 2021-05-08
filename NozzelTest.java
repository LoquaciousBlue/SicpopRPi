public class NozzelTest {
    public static void main(String[] args) {
        int numNozzelsToFire = 0;
        SicpopPrinter sp = new SicpopPrinter();
        
        try {
            if (args.length != 1){
                System.out.println("Need to provide a number as argument");
                System.exit(0);
                numNozzelsToFire = Integer.parseInt(args[0]);
            }
        } catch (NumberFormatException e){
            System.out.println("...that isn't a number edwin...");
            System.exit(0);
        }
        
        sp.spray(numNozzelsToFire);
    }
}
