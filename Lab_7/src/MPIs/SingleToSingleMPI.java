package MPIs;

public class SingleToSingleMPI extends MatrixMPI {
    public SingleToSingleMPI(int[][] a, int[][] b) {
        super(a, b);
    }

    @Override
    public int[][] multiply() {
        var result = new int[aRow][bCol];
        for (var arow = 0; arow < aRow; arow++) {
            for (var i = 0; i < aCol; i++) {
                for (var j = 0; j < bCol; j++) {
                    result[arow][i] = a[arow][j] * b[j][i];
                }
            }
        }
        return result;
    }
}
