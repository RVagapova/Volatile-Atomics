import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class Main {

    private static final AtomicInteger length3 = new AtomicInteger();
    private static final AtomicInteger length4 = new AtomicInteger();
    private static final AtomicInteger length5 = new AtomicInteger();

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        String[] texts = new String[100_000];
        for (int i = 0; i < texts.length; i++) {
            texts[i] = generateText("abc", 3 + random.nextInt(3));
        }

        CountDownLatch latch = new CountDownLatch(3);


        Runnable beautyCheck1 = createRunnableBeautyCheck(str -> {
            int i = 0;
            while (i < str.length() / 2) {
                if (str.charAt(i) != str.charAt(str.length() - i - 1)) {
                    return false;
                }
                i++;
            }
            return true;
        }, texts, latch);

        Runnable beautyCheck2 = createRunnableBeautyCheck(str -> {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(0) != str.charAt(i)) {
                    return false;
                }
            }
            return true;
        }, texts, latch);

        Runnable beautyCheck3 = createRunnableBeautyCheck(str -> {
            for (int i = 0; i < str.length() - 1; i++) {
                if (str.charAt(i) > str.charAt(i + 1)) {
                    return false;
                }
            }
            return true;
        }, texts, latch);

        executor.submit(beautyCheck1);
        executor.submit(beautyCheck2);
        executor.submit(beautyCheck3);

        latch.await();

        executor.shutdown();

        System.out.println("3 = " + length3.get());
        System.out.println("4 = " + length4.get());
        System.out.println("5 = " + length5.get());
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static void counterIncrement(String str) {
        int length = str.length();
        if (length == 3) {
            length3.incrementAndGet();
        } else if (length == 4) {
            length4.incrementAndGet();
        } else {
            length5.incrementAndGet();
        }
    }

    private static Runnable createRunnableBeautyCheck(Predicate<String> predicate, String[] texts, CountDownLatch latch) {
        return () -> {
            Arrays.stream(texts)
                    .parallel()
                    .filter(predicate)
                    .forEach(Main::counterIncrement);
            latch.countDown();
        };
    }

}