import java.io.BufferedWriter;
import java.io.FileWriter;

public class Benchmark {


    public static void main(String[] args) {

        double mean_hamming = 0.0;
        double mean_spatial_entropy=0.0;
        double mean_temporal_entropy=0.0;
        CellularAutomata1D aut = new CellularAutomata1D();


        for (int r = 0; r < 256; r++) {
            System.out.println("Procesando regla: " + r + "\n");

            for(int i = 0; i < 23; i++) {
                aut = new CellularAutomata1D();
                aut.setTransitionFunction(r);
                aut.computeRule();
                aut.initializer(1000, 4000, 2, 1,
                        1, 1, 0, "generatorFishmanAndMore2", 499);


                aut.caComputation(4000);
                mean_spatial_entropy += aut.getSpatialEntropyValue()/4000;
                mean_hamming += aut.getHammingDistanceValue()/4000;
                mean_temporal_entropy = aut.getTemporalEntropy();

            }
            System.out.println("Processing rule: " + r + "\n");


            mean_spatial_entropy = mean_spatial_entropy/23;
            mean_hamming = mean_hamming/23;
            mean_temporal_entropy = mean_temporal_entropy/23;

            if(mean_spatial_entropy > 0.85 && mean_hamming > 500 && mean_temporal_entropy > 0.9)
            {
                String rule = new String("Rule: "+ r+ "accepted");

                BufferedWriter rules_accepted_file = null;
                try {

                    rules_accepted_file = new BufferedWriter(new FileWriter("rules_accepted.txt",true));
                    rules_accepted_file.append(rule).append("\n");
                    rules_accepted_file.close();

                } catch (Exception ex) {
                    System.out.println("Exception message : " + ex.getMessage());
                }
            }
        }

    }

}
