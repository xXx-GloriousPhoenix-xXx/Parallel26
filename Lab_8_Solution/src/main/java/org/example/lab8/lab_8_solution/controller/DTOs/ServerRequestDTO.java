package org.example.lab8.lab_8_solution.controller.DTOs;

public class ServerRequestDTO {
    private int matrixSize;
    private int minValue;
    private int maxValue;

    public int getMatrixSize() { return this.matrixSize; }
    public int getMinValue() { return this.minValue; }
    public int getMaxValue() { return this.maxValue; }

    public void setMatrixSize(int matrixSize) { this.matrixSize = matrixSize; }
    public void setMinValue(int minValue) { this.minValue = minValue; }
    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }
}
