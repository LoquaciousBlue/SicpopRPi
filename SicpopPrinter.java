import java.util.concurrent.atomic.AtomicBoolean;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;


public class SicpopPrinter implements Printer {
    private final int X_MOTOR = 0;
    private final int Y_MOTOR = 1;
    private final int X_DIREC = 2;
    private final int Y_DIREC = 3;

    // Data Structures
    private AtomicBoolean isOffCooldown;
    private int posx, posy;
    private Thread cooldownThread;

    // Virtual Hardware
    private final GpioController gpioContr;
    private final GpioPinDigitalOutput[] motorPins, nozzlePins;
    private final Pin gpioP0 = RaspiPin.GPIO_00, gpioP1 = RaspiPin.GPIO_01, 
        gpioP2 = RaspiPin.GPIO_02, gpioP3 = RaspiPin.GPIO_03, gpioP4 = RaspiPin.GPIO_04, 
        gpioP5 = RaspiPin.GPIO_05, gpioP6 = RaspiPin.GPIO_06, gpioP7 = RaspiPin.GPIO_07, 
        gpioP8 = RaspiPin.GPIO_08, gpioP9 = RaspiPin.GPIO_09, gpioPA = RaspiPin.GPIO_10, 
        gpioPB = RaspiPin.GPIO_11, gpioPC = RaspiPin.GPIO_12, gpioPD = RaspiPin.GPIO_13,
        gpioPE = RaspiPin.GPIO_14, gpioPF = RaspiPin.GPIO_15;

    /** 
     * 
    */
    public SicpopPrinter(){
        super();
        this.posx = 0;
        this.posy = 0;
        this.isOffCooldown.set(true);
        this.cooldownThread = new Thread(()->{
            long t = System.nanoTime() + 1000;
            while(t > System.nanoTime());
            this.isOffCooldown.set(true);
        });
        this.motorPins = new GpioPinDigitalOutput[4];
        this.nozzlePins = new GpioPinDigitalOutput[12];
        this.gpioContr = GpioFactory.getInstance();

        // Manual Init:
        this.motorPins[X_MOTOR] = gpioContr.provisionDigitalOutputPin(gpioPC);
        this.motorPins[Y_MOTOR] = gpioContr.provisionDigitalOutputPin(gpioPD);
        this.motorPins[X_DIREC] = gpioContr.provisionDigitalOutputPin(gpioPE);
        this.motorPins[Y_DIREC] = gpioContr.provisionDigitalOutputPin(gpioPF);
        this.nozzlePins[0] = gpioContr.provisionDigitalOutputPin(gpioP5);
        this.nozzlePins[1] = gpioContr.provisionDigitalOutputPin(gpioP6);
        this.nozzlePins[2] = gpioContr.provisionDigitalOutputPin(gpioP7);
        this.nozzlePins[3] = gpioContr.provisionDigitalOutputPin(gpioP8);
        this.nozzlePins[4] = gpioContr.provisionDigitalOutputPin(gpioPB);
        this.nozzlePins[5] = gpioContr.provisionDigitalOutputPin(gpioPA);
        this.nozzlePins[6] = gpioContr.provisionDigitalOutputPin(gpioP9);
        this.nozzlePins[7] = gpioContr.provisionDigitalOutputPin(gpioP2);
        this.nozzlePins[8] = gpioContr.provisionDigitalOutputPin(gpioP1);
        this.nozzlePins[9] = gpioContr.provisionDigitalOutputPin(gpioP0);
        this.nozzlePins[10] = gpioContr.provisionDigitalOutputPin(gpioP3);
        this.nozzlePins[11] = gpioContr.provisionDigitalOutputPin(gpioP4);
    }

    public void print(Instructions instr){
        // Setup (should be redundant)
        for (GpioPinDigitalOutput gpio : this.motorPins) gpio.low();
        for (GpioPinDigitalOutput gpio : this.nozzlePins) gpio.low();

        // Loop
        for (Pair<PrintCommands, Integer> p : instr){
            // Movement
            this.updatePosition(p.getFirst());

            // Nozzle Fire
            if(p.getSecond() > 0){
                while(!this.isOffCooldown.get())/* busy wait for nozzle cooldown to finish*/;
                this.isOffCooldown.set(false);
                this.spray(p.getSecond());
            }
        }
        this.reset();
    }

    private void updatePosition(PrintCommands pc){
        switch(pc){
            case ADVANCE:
                this.advance();
                break;
            case FEED:
                this.feed();
                break;
            default:
                break; // Do nothing
        }
    }

    private void advance(){
        this.motorPins[X_MOTOR].high();
        this.busyWaitMicro(5);
        this.motorPins[X_MOTOR].low();
        this.busyWaitMicro(100);
        if (this.motorPins[X_DIREC].getState().isLow())
            posx++;
        else
            posx--;
    }

    private void feed(){
        this.motorPins[Y_MOTOR].high();
        this.busyWaitMicro(5);
        this.motorPins[Y_MOTOR].low();
        this.busyWaitMicro(100);
        this.motorPins[X_DIREC].toggle();
        posy++;
    }

    private void reset(){
        this.motorPins[X_DIREC].high(); // Reverse Motor
        this.motorPins[Y_DIREC].high(); // Reverse Motor

        while (posx-- > 0) this.advance();
        while (posy-- > 0) this.feed();

        this.motorPins[X_DIREC].low(); // Forward Motor
        this.motorPins[Y_DIREC].low(); // Forward Motor
        posx = 0;
        posy = 0;
    }

    public void spray(int numOfNozzles){
        int numNoz = (numOfNozzles < 12 ? numOfNozzles : 12);
        for (int i = 0; i < numNoz; i++){
            this.nozzlePins[i].high();
            this.busyWaitMicro(1);
            this.nozzlePins[i].low();
            this.busyWaitMicro(5);
        }
        this.cooldownThread.run();
    }

    // Busy wait for micro-second level timing.
    private void busyWaitMicro(long us){
        long waitUntil = System.nanoTime() + (us*1000);
        while(waitUntil > System.nanoTime())/* actively wait; no sleeping! */ ;
    }

    private void busyWaitMicroManual(long waitUntil){
        while(waitUntil > System.nanoTime());
    }

}
