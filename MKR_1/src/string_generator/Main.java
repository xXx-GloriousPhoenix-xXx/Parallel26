package string_generator;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final List<String> collection = Collections.synchronizedList(new ArrayList<>());
    private static final int TARGET_SIZE = 10000;

    public static void main(String[] args) {
        for (var i = 0; i < 7; i++) {
            final var threadId = i;
            new Thread(() -> {
                var length = threadId + 1;

                while (collection.size() < TARGET_SIZE) {
                    var randomString = generate(wordLength);

                    if (collection.size() < TARGET_SIZE) {
                        collection.add(randomString);
                    }
                }
                System.out.println("Потік " + threadId + " завершив роботу.");
            }).start();
        }
    }

    private static String generate(int length) {
        var chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        var sb = new StringBuilder();
        var random = ThreadLocalRandom.current();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}