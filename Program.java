public class Program
{
    public static void main(String[] args) throws InterruptedException {
        long start = System.nanoTime(), end;
		Thread.sleep(0, 500000);
        end = System.nanoTime();
        System.out.printf("%d\n", end - start);
	}
}