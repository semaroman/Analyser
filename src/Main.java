import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static BlockingQueue<String> queue1 = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queue2 = new ArrayBlockingQueue<>(100);
    public static BlockingQueue<String> queue3 = new ArrayBlockingQueue<>(100);
    public static Thread textGen;

    public static void main(String[] args) throws InterruptedException {
        textGen = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                String text = genText("abc", 100_000);
                try {
                    queue1.put(text);
                    queue2.put(text);
                    queue3.put(text);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        textGen.start();

        Thread a = getThread(queue1, 'a');
        Thread b = getThread(queue2, 'b');
        Thread c = getThread(queue3, 'c');

        a.start();
        b.start();
        c.start();

        a.join();
        b.join();
        c.join();
    }

    public static String genText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    public static Thread getThread(BlockingQueue<String> queue, char letter) {
        return new Thread(() -> {
            int max = maxLetters(queue, letter);
            System.out.println("Максимальное количество букв \"" + letter + "\" во всём тексте: " + max + " шт.");
        });
    }

    public static int maxLetters(BlockingQueue<String> queue, char letter) {
        int count = 0;
        int max = 0;
        String text;
        try {
            while (textGen.isAlive()) {
                text = queue.take();
                for (char c : text.toCharArray()) {
                    if (c == letter) {
                        count++;
                    }
                }
                if (count > max) {
                    max = count;
                }
                count = 0;
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " был прерван");
            return -1;
        }
        return max;
    }
}
