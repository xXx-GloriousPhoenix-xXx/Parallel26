package file.search;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class KeywordSearchTask extends RecursiveTask<List<Path>> {

    private final List<Path> files;
    private final Set<String> keywords;
    private final int threshold;

    public KeywordSearchTask(List<Path> files, Set<String> keywords, int threshold) {
        this.files = files;
        this.keywords = keywords;
        this.threshold = threshold;
    }

    @Override
    protected List<Path> compute() {
        if (files.size() <= threshold) {
            return processSequentially();
        }

        int mid = files.size() / 2;
        var left = new KeywordSearchTask(files.subList(0, mid), keywords, threshold);
        var right = new KeywordSearchTask(files.subList(mid, files.size()), keywords, threshold);

        left.fork();
        List<Path> rightResult = right.compute();
        List<Path> leftResult = left.join();

        leftResult.addAll(rightResult);
        return leftResult;
    }

    private List<Path> processSequentially() {
        List<Path> result = new ArrayList<>();

        for (Path file : files) {
            if (containsKeyword(file)) {
                result.add(file);
            }
        }
        return result;
    }

    private boolean containsKeyword(Path file) {
        try {
            String text = Files.readString(file).toLowerCase();
            for (String k : keywords) {
                if (text.contains(k)) return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
