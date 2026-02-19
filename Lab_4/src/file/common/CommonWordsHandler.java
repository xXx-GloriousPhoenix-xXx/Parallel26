package file.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class CommonWordsHandler {
    private static final Path WORK_DIR =
            Path.of("F:\\Programmes\\Github\\Reps\\Parallel26\\Lab_4\\src\\file_workspace");

    public static Set<String> search(List<Path> files) {
        var pool = new ForkJoinPool();
        var common = pool.invoke(new CommonWordsTask(files, 2));
        pool.shutdown();
        return common;
    }

    public static void handle() {
        try {
            List<Path> files = Files.list(WORK_DIR)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            System.out.println("=== Спільні слова ===");
            long start = System.currentTimeMillis();

            var result = search(files);

            long end = System.currentTimeMillis();

            System.out.println("Кількість спільних слів: " + result.size());
            System.out.println("Спільні слова:");
            result.forEach(w -> System.out.print(w + " "));
            System.out.println();

            System.out.println("Час: " + (end - start) + " мс");
            System.out.println();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
