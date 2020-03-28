import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
     * ClassNV.java
     * Purpose: generic Class that you can modify and adapt easily for any application
     * that need data visualization.
     * @author: Jeffrey Pallarés Núñez.
     * @version: 1.0 23/07/19
     */



public class CellularAutomata1D implements Runnable
{

    private static int[][] matrix;
    public static AtomicIntegerArray population_counter;
    private static AtomicInteger hamming_distance_counter;
    private int [] local_population_counter;
    private int local_hamming_distance_counter;
    private static LinkedList<Double>[] population;
    private static LinkedList<Double> hamming;
    private static LinkedList<Double> spatial_entropy;
    private static double temporal_entropy;
    private static int[] temporal_entropy_counter;
    public static MainCanvas canvasTemplateRef;
    public static AnalyticsMultiChart population_chart_ref;
    public int[][] getData() { return matrix; }
    public void plug(MainCanvas ref) { canvasTemplateRef = ref; }
    public void plugPopulationChart(AnalyticsMultiChart ref) { population_chart_ref = ref;}
    public static int entropy_cell;

    private static int width, height;

    public static int states_number = 2;
    private static int neighborhood_range = 1;
    private static int transition_function = 1;
    private static int cfrontier = 0;
    private static  RandomGenerator randomInitializer;
    private static EngineGenerator handler = new EngineGenerator();
    private static String random_engine;
    private static int seq_len;
    private static int seed;
    private static int cells_number;
    public static int generations;

    private static int[] binary_rule;
    private int task_number;
    private static int total_tasks;
    private static CyclicBarrier barrier = null;
    private int in;
    private int fn;
    public static Boolean abort = false;
    private static int gens;
    private static int size_pool;
    private static ThreadPoolExecutor myPool;



    public void run() {

        for (int i = 0; i < generations-1 ; i++) {
            if(abort)
                break;
            nextGen(i);

            try
            {
                int l = barrier.await();
                for (int j = 0; j < states_number; j++) {
                    population_counter.getAndAdd(j,this.local_population_counter[j]);
                }

                hamming_distance_counter.addAndGet(this.local_hamming_distance_counter);

                if(barrier.getParties() == 0)
                    barrier.reset();

                l = barrier.await();


                if(this.task_number==1) {
                    this.canvasTemplateRef.revalidate();
                    this.canvasTemplateRef.repaint();
                    Thread.sleep(0,10);

                    int[] spatial_entropy_counter = new int [states_number];

                    for (int j = 0; j < states_number; j++) {
                        spatial_entropy_counter[j] = population_counter.get(j);
                        population[j].add((double)population_counter.get(j));
                    }
                    population_counter = new AtomicIntegerArray(states_number);
                    hamming.add((double)hamming_distance_counter.intValue());
                    hamming_distance_counter = new AtomicInteger(0);
                    spatial_entropy.add(computeEntropy(spatial_entropy_counter));
                    CellularAutomata1D.population_chart_ref.plot();
                }

                if(barrier.getParties() == 0)
                    barrier.reset();

                l = barrier.await();


                if(barrier.getParties() == 0)
                    barrier.reset();
            }catch(Exception e){}
        }

        if(this.task_number==1)
        temporal_entropy = computeEntropy(temporal_entropy_counter);


    }

    public CellularAutomata1D(){}

    public CellularAutomata1D(int i) {
        task_number = i;

        int paso = cells_number /total_tasks;


        fn = paso * task_number;
        in = fn - paso;

        if( total_tasks == task_number)
            fn =cells_number;

        System.out.println(in+" "+fn);

    }

    public static void next_gen_concurrent(int nt,int g) {
        gens =g;

        size_pool =nt;

        barrier = new CyclicBarrier (size_pool);
        total_tasks = size_pool;

        myPool = new ThreadPoolExecutor(
                size_pool, size_pool, 60000L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        CellularAutomata1D[] tareas = new  CellularAutomata1D[nt];

        for(int t = 0; t < nt; t++)
        {
            tareas[t] = new CellularAutomata1D(t+1);
            myPool.execute(tareas[t]);

        }

        myPool.shutdown();
        try{
            myPool.awaitTermination(10, TimeUnit.HOURS);
        } catch(Exception e){
            System.out.println(e.toString());
        }

    }

    public LinkedList<Double>[] getPopulation(){
        return population;
    }
    public LinkedList<Double> getHammingDistance(){
        return hamming;
    }
    public LinkedList<Double> getEntropy(){
        return spatial_entropy;
    }
    public Double getTemporalEntropy(){
        return temporal_entropy;
    }
    public double computeEntropy(int[] population){
        double entropy = 0.0;
        for(int symbol: population){
            double probability = (symbol+0.0)/cells_number;
            if(probability !=0)
                entropy += probability * Math.log(probability);
        }
        return  entropy*-1;
    }

    private int[] compute_rule(){

        int decimal_rule = transition_function;
        int size_binary_rule = (2*neighborhood_range+1)*states_number;
        binary_rule  = new int[size_binary_rule];

        for( int i = 0; i < size_binary_rule ; i++ )
        {
            binary_rule[i] = decimal_rule % states_number;
            decimal_rule = decimal_rule / states_number;
        }



        StringBuilder cout= new StringBuilder(new String());
        cout.append("| ");
        for (int value : binary_rule) {
            cout.append(value);
            cout.append(" | ");
        }
        System.out.println(cout);
        System.out.println(Arrays.toString(binary_rule));
        return binary_rule;
    }

    private void initializeState(ArrayList<BigInteger> random_generated){
        for(BigInteger num: random_generated){
            matrix[num.intValue()%width][0] = num.intValue()%states_number;
        }
    }


    public void initializer (int cells_number, int generations, int states_number,
                             int neighborhood_range, int transition_function, int seed,
                             int cfrontier , String random_engine, int entropy_cell){
        width = cells_number;
        height = generations;
        matrix = new int[height][width];
        CellularAutomata1D.entropy_cell = entropy_cell;

        population_counter = new AtomicIntegerArray(states_number);
        hamming_distance_counter = new AtomicInteger(0);
        temporal_entropy_counter = new int[states_number];

        CellularAutomata1D.cells_number = cells_number;
        CellularAutomata1D.generations = generations;
        CellularAutomata1D.states_number = states_number;
        CellularAutomata1D.neighborhood_range = neighborhood_range;
        CellularAutomata1D.transition_function = transition_function;
        CellularAutomata1D.cfrontier = cfrontier;
        CellularAutomata1D.random_engine = random_engine;
        CellularAutomata1D.seed = seed;

        population = new LinkedList[states_number];
        hamming = new LinkedList<Double>();
        spatial_entropy = new LinkedList<Double>();
        for (int i = 0; i < states_number; i++) {
            population[i] = new LinkedList<Double>();
        }
        compute_rule();
        handler.createEngines();
        randomInitializer = new RandomGenerator(seed);

        if (random_engine.equals("Basic"))
            matrix[width / 2][0] = 1;
        else if(!random_engine.equals("generatorCombinedWXY")) {
            ArrayList<BigInteger> random_generated = randomInitializer.
                    getRandomSequence(handler.engines.get(random_engine), seed, width);
            initializeState(random_generated);
        }
        else {
            ArrayList<BigInteger> random_generated = randomInitializer.
                    getRandomSequenceCombined(handler.combined_engines.get(random_engine),
                            handler.engines.get("generatorCombinedW"), handler.engines.get("generatorCombinedY"),
                            handler.engines.get("generatorCombinedX"),
                            seed, seed, seed, width);
            initializeState(random_generated);
        }
        temporal_entropy_counter[matrix[0][entropy_cell]]++;
    }


    public static void stop() {
        abort = true;
    }

    public static LinkedList<Double>[]caComputation(int nGen){
        abort = false;
        generations = nGen;
        next_gen_concurrent(4,nGen);

        return population;
    }

    public  LinkedList<Double>[] nextGen(int actual_gen){

        local_population_counter = new int[states_number];
        local_hamming_distance_counter = 0;
        for (int i = 0; i < states_number; i++) {
            this.local_population_counter[i]=0;

        }
        if (cfrontier==0){
            for (int i = in; i < fn; i++) {
                if(abort)
                    break;
                int j =(i + neighborhood_range) % width;
                int irule = 0;
                int exp = 0;

                while(exp < neighborhood_range *2 +1){
                    if(j<cells_number && j>0)
                        irule = irule + matrix[j][actual_gen]  * (int)Math.pow(states_number,exp);
                    exp ++;
                    j = ( j== 0) ? 0 : j - 1;
                }

                if (irule >= binary_rule.length)
                    matrix[i][actual_gen + 1] = 0;
                else
                    matrix[i][actual_gen + 1] = binary_rule[irule];

                local_population_counter[matrix[i][actual_gen + 1]]++;
                if( matrix[i][actual_gen] != matrix[i][actual_gen+1])
                    local_hamming_distance_counter++;

                if(i == entropy_cell){
                    temporal_entropy_counter[matrix[i][actual_gen + 1]]++;
                }

            }

        }
        else{
            for (int i = in; i < fn; i++) {
                if(abort)
                    break;
                int j =(i + neighborhood_range) % width;
                int irule = 0;
                int exp = 0;

                while(exp < neighborhood_range *2 +1){
                    irule = irule + matrix[j][actual_gen] * (int)Math.pow(states_number,exp);
                    exp ++;
                    j = ( j== 0) ? ( j - 1 + cells_number) : j - 1;
                }

                if (irule >= binary_rule.length)
                    matrix[i][actual_gen + 1] = 0;
                else
                    matrix[i][actual_gen + 1] = binary_rule[irule];

                local_population_counter[matrix[i][actual_gen + 1]]++;

                if( matrix[i][actual_gen] != matrix[i][actual_gen+1])
                    local_hamming_distance_counter++;

                if(i == entropy_cell){
                    temporal_entropy_counter[matrix[i][actual_gen + 1]]++;
                }


            }


        }
        return population;

    }

}