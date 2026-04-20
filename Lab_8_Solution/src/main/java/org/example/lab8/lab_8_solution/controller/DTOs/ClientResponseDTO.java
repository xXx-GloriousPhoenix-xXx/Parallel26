package org.example.lab8.lab_8_solution.controller.DTOs;

public class ClientResponseDTO {
    public int matrixSize;
    public double receiveTimeMs;
    public double calculateTimeMs;
    public int[][] resultMatrix;

    public ClientResponseDTO(int matrixSize, double receiveTimeMs, double calculateTimeMs, int[][] resultMatrix) {
        this.matrixSize = matrixSize;
        this.receiveTimeMs = receiveTimeMs;
        this.calculateTimeMs = calculateTimeMs;
        this.resultMatrix = resultMatrix;
    }
}
