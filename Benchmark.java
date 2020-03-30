public class Benchmark {


    public static void main(String[] args) {

        double[] maxg = new double[3];

        double maxl = 0.0;
        double enmedia = 0.0;
        CellularAutomata1D aut = new CellularAutomata1D();


        for (int r = 0; r < 256; r++) {
            System.out.println("Procesando regla: " + r + "\n");

            for(int i = 0; i < 23; i++) {
                aut = new CellularAutomata1D();
                enmedia = 0;
                aut.setTransitionFunction(r);
                aut.computeRule();
                aut.initializer(1000, 4000, 2, 1,
                        1, 1, 0, "generatorFishmanAndMore2", 499);


                aut.caComputation(4000);

            }
            System.out.println("Procesando regla: " + r + "\n");


            maxl = aut.getTemporalEntropy();
            enmedia = enmedia / 4000;


            if (maxl > maxg[1] && enmedia > 0.9997) {
                maxg[0] = r;
                maxg[1] = maxl;
                maxg[2] = enmedia;
            }

        }

        System.out.println("la mejor regla es: " + maxg[0] + " Con una Entropia temporal de: " + maxg[1]);
        System.out.println("la mejor regla es: " + maxg[0] + " Con una Entropia temporal de: " + maxg[2]);
    }

}
