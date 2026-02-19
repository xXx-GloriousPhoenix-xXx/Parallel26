package file;

import file.factory.FileGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FileHandler {
    void main() throws IOException {
        var generator = new FileGenerator();

        var commonWords = List.of(
                "system",
                "data",
                "process"
        );

        var keywords = List.of(
                "algorithm",
                "database",
                "network",
                "compiler",
                "thread",
                "java"
        );

        var dictionary = List.of(
                "tree","graph","cloud","memory","cpu","disk",
                "kernel","cache","vector","matrix","file",
                "random","input","output","code","program"
        );

        generator.generate(
                Path.of("F:\\Programmes\\Github\\Reps\\Parallel26\\Lab_4\\src\\file_workspace"),
                20,
                200,
                ".txt",
                commonWords,
                keywords,
                dictionary
        );

        System.out.println("Файли створені");
    }
}
