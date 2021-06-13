/**
 * El codigo de esta clase ha sido cogido de esta web para implementar el calculo.
 * http://es.uwenku.com/question/p-smbibclk-o.html
 * Se ha añadido la respuesta como a complementacion y se han hecho pruebas de su funcionamiento
 */
public class DCT {
    private static final int N = 8; // Bloques de 8 x 8
    private double[] c = new double[N];
    private static final double block = 8.0;

    public DCT() {
        this.initializeCoefficients();
    }

    private void initializeCoefficients() {
        for (int i = 1; i < N; i++) {
            c[i] = 1;
        }
        c[0] = 1 / Math.sqrt(block);
    }

    public int[][] applyDCT(int[][] f) {
        int[][] F = new int[N][N];
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0;
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        sum += Math.cos(((2 * i + 1) / (2.0 * N)) * u * Math.PI) * Math.cos(((2 * j + 1) / (2.0 * N)) * v * Math.PI) * f[i][j];
                    }
                }
                sum *= ((c[u] * c[v]) / 4);
                F[u][v] = (int) sum;
            }
        }
        return F;
    }

    /*public double[][] applyIDCT(double[][] F) {
        double[][] f = new double[N][N];
        for (int i=0;i<N;i++) {
            for (int j=0;j<N;j++) {
                double sum = 0.0;
                for (int u=0;u<N;u++) {
                    for (int v=0;v<N;v++) {
                        sum+=(c[u]*c[v])/4.0*Math.cos(((2*i+1)/(2.0*N))*u*Math.PI)*Math.cos(((2*j+1)/(2.0*N))*v*Math.PI)*F[u][v];
                    }
                }
                f[i][j]=Math.round(sum);
            }
        }
        return f;
    }*/
}