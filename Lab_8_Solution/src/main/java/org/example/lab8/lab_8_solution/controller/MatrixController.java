package org.example.lab8.lab_8_solution.controller;

import org.example.lab8.lab_8_solution.controller.DTOs.ClientResponseDTO;
import org.example.lab8.lab_8_solution.controller.DTOs.ClientRequestDTO;
import org.example.lab8.lab_8_solution.controller.DTOs.ServerRequestDTO;
import org.example.lab8.lab_8_solution.controller.DTOs.ServerResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class MatrixController {
    public static final int WARMUPS = 3;
    public static final int RUNS = 10;

    private record TimingResult(double generateTimeMs, double calculateTimeMs) {}

    @PostMapping("/perform/client")
    public ClientResponseDTO performClient(@RequestBody ClientRequestDTO request) {
        var startReceive = System.nanoTime();
        var a = request.getA();
        var b = request.getB();
        var endReceive = System.nanoTime();
        var receiveTime = (endReceive - startReceive) / 1_000_000.0;

        var startCalculate = System.nanoTime();
        var c = MatrixService.multiplyParallel(a, b);
        var endCalculate = System.nanoTime();
        var calculateTime = (endCalculate - startCalculate) / 1_000_000.0;

        return new ClientResponseDTO(a.length, receiveTime, calculateTime, c);
    }

    @PostMapping("/perform/server")
    public ServerResponseDTO performServer(@RequestBody ServerRequestDTO request) {
        var n = request.getMatrixSize();
        var min = request.getMinValue();
        var max = request.getMaxValue();

        for (var warmup = 0; warmup < WARMUPS; warmup++) {
            performServerSingle(n, min, max);
        }

        var generateTimesMs = new double[RUNS];
        var calculateTimesMs = new double[RUNS];
        for (var run = 0; run < RUNS; run++) {
            var timingResult = performServerSingle(n, min, max);
            generateTimesMs[run] = timingResult.generateTimeMs;
            calculateTimesMs[run] = timingResult.calculateTimeMs;
        }

        var avgGenerateTimeMs = Arrays.stream(generateTimesMs)
                .average()
                .orElse(0.0);
        var avgCalculateTimeMs = Arrays.stream(calculateTimesMs)
                .average()
                .orElse(0.0);
        return new ServerResponseDTO(n, avgGenerateTimeMs, avgCalculateTimeMs);
    }

    public TimingResult performServerSingle(int n, int min, int max) {
        var startGenerate = System.nanoTime();
        var a = MatrixService.generateMatrix(n, min, max);
        var b = MatrixService.generateMatrix(n, min, max);
        var endGenerate = System.nanoTime();
        var generateTimeMs = (endGenerate - startGenerate) / 1_000_000.0;

        var startCalculate = System.nanoTime();
        MatrixService.multiplyParallel(a, b);
        var endCalculate = System.nanoTime();
        var calculateTimeMs = (endCalculate - startCalculate) / 1_000_000.0;

        return new TimingResult(generateTimeMs, calculateTimeMs);
    }
}
