package file.search;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class KeywordSearchHandler {

    private static final Path WORK_DIR =
            Path.of("F:\\Programmes\\Github\\Reps\\Parallel26\\Lab_4\\src\\file_workspace");

    public static List<Path> search(Set<String> keywords, List<Path> files) {
        var pool = new ForkJoinPool();
        var keywordFiles = pool.invoke(new KeywordSearchTask(files, keywords, 2));
        pool.shutdown();
        return keywordFiles;
    }

    public static void handle() {

        try {
            List<Path> files = Files.list(WORK_DIR)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            Set<String> keywords = Set.of(
                    "algorithm",
                    "database",
                    "network",
                    "compiler",
                    "thread",
                    "java"
            );

            System.out.println("=== Пошук за ключовими словами ===");
            System.out.println("Ключові слова: " + keywords);

            long start = System.currentTimeMillis();

            List<Path> result = search(keywords, files);

            long end = System.currentTimeMillis();

            System.out.println("Знайдені файли: " + result.size());
            result.forEach(p -> System.out.println(p.getFileName()));

            System.out.println("Час: " + (end - start) + " мс");
            System.out.println();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
