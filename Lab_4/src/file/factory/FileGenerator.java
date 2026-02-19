package file.factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FileGenerator {
    private final Random random = new Random();
    public void generate(
            Path directory,
            int fileCount,
            int wordsPerFile,
            String extension,
            List<String> commonWords,
            List<String> keywords,
            List<String> dictionary
    ) throws IOException {

        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        for (int i = 0; i < fileCount; i++) {

            var file = directory.resolve("file_" + i + extension);

            var content = new ArrayList<>(commonWords);

            if (random.nextBoolean()) {
                int kCount = random.nextInt(keywords.size()) + 1;
                var shuffled = new ArrayList<>(keywords);
                Collections.shuffle(shuffled);
                content.addAll(keywords.subList(0, kCount));
            }

            while (content.size() < wordsPerFile) {
                content.add(dictionary.get(random.nextInt(dictionary.size())));
            }

            Collections.shuffle(content);

            Files.writeString(file, String.join(" ", content));
        }
    }
}
