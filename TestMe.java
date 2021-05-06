import java.util.Arrays;

public class TestMe {

    public static void main(String[] args) throws InterruptedException {
        long[] samples = new long[1_000_000];

        for (int i = 0; i < samples.length; i++) {
            long firstTime = System.nanoTime();
            long timeForNano = System.nanoTime() - firstTime;
            samples[i] = timeForNano;
        }

        System.out.printf("Time for call to nano %.0f nanseconds", Arrays.stream(samples).average().getAsDouble());
    }
}
