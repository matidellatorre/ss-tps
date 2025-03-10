public class Constants {
    private static int M;
    private static int N;
    private static double L;
    private static double Rc;
    private static double cellLen;
    private static boolean boundaryCond;

    private Constants() {}

    public static void initialize(int m, int n, double l, double rc, boolean boundaryC){
        M = m;
        N = n;
        L = l;
        Rc = rc;
        cellLen = l/m;
        boundaryCond = boundaryC;
    }

    public static int getM() {
        return M;
    }

    public static int getN() {
        return N;
    }

    public static double getL() {
        return L;
    }

    public static double getRc() {
        return Rc;
    }

    public static double getCellLen() {
        return cellLen;
    }

    public static boolean getBoundaryCond() {
        return boundaryCond;
    }
}
