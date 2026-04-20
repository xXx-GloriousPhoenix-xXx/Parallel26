package org.example.lab8.lab_8_solution.controller.DTOs;

public class ServerResponseDTO {
    public int matrixSize;
    public double generateTimeMs;
    public double calculateTimeMs;

    public ServerResponseDTO(int matrixSize, double generateTimeMs, double calculateTimeMs) {
        this.matrixSize = matrixSize;
        this.generateTimeMs = generateTimeMs;
        this.calculateTimeMs = calculateTimeMs;
    }
}
