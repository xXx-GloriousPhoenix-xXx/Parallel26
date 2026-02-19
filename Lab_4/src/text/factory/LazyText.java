package text.factory;

import java.util.Random;

public class LazyText implements ILazyText {
    private final int size;
    private final int maxLength;
    private final Random seedRandom;

    public LazyText(int size, int maxLength) {
        this.size = size;
        this.maxLength = maxLength;
        this.seedRandom = new Random(42);
    }

    public int getSize() { return size; }

    public String getWord(int index) {
        var random = new Random(seedRandom.nextLong() + index);
        var length = random.nextInt(maxLength) + 1;
        var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}