package file.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class CommonWordsTask extends RecursiveTask<Set<String>> {

    private final List<Path> files;
    private final int threshold;

    public CommonWordsTask(List<Path> files, int threshold) {
        this.files = files;
        this.threshold = threshold;
    }

    @Override
    protected Set<String> compute() {
        if (files.size() <= threshold) {
            return processSequentially();
        }

        var mid = files.size() / 2;
        var leftTask = new CommonWordsTask(files.subList(0, mid), threshold);
        var rightTask = new CommonWordsTask(files.subList(mid, files.size()), threshold);

        leftTask.fork();
        var right = rightTask.compute();
        var left = leftTask.join();

        left.retainAll(right);
        return left;
    }

    private Set<String> processSequentially() {
        Set<String> result = null;

        for (Path file : files) {
            var words = readWords(file);

            if (result == null) {
                result = new HashSet<>(words);
            } else {
                result.retainAll(words);
            }
        }
        return result == null ? new HashSet<>() : result;
    }

    private Set<String> readWords(Path file) {
        try {
            var text = Files.readString(file).toLowerCase();
            text = text.replaceAll("[^a-zа-я0-9 ]", " ");
            return new HashSet<>(Arrays.asList(text.split("\\s+")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
