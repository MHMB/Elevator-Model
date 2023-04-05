package institute.teias.ds;

import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.drivers.reflect.MethodOutput;
import de.learnlib.drivers.reflect.SimplePOJOTestDriver;
import de.learnlib.filter.cache.sul.SULCaches;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.oracle.equivalence.MealyRandomWordsEQOracle;
import de.learnlib.mapper.SULMappers;
import de.learnlib.mapper.StringMapper;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.statistics.SimpleProfiler;
import institute.teias.ds.elevator.Elevator;
import institute.teias.ds.elevator.ElevatorControlSystem;
import institute.teias.ds.elevator.exceptions.InvalidNumber;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import de.learnlib.drivers.reflect.MethodInput;

import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import static institute.teias.ds.util.Dot.saveMealyMachineAsDot;

public class Main {
    private static final double RESET_PROBABILITY = 0.05;
    private static final int MAX_STEPS = 100000;
    private static final int RANDOM_SEED = 46_346_293;
    private static final long timestamp = System.currentTimeMillis();
    private static final String outDir = "output/";
    private static final String outputFile = outDir + "out.txt";
    private static final String statisticsFile = outDir + "statistics.txt";
    private static final String learnedModelFile = outDir + timestamp +"learnresult.dot";
    private static final String hypFile(int hypNum) {
        return outDir + "hypothesis-" + hypNum + ".dot";
    }
    private PrintStream statisticsFileStream;


    public static void learnElevator(Constructor<?> elevConst, Object elevParam1) throws Exception{
        SimplePOJOTestDriver driver = new SimplePOJOTestDriver(elevConst, elevParam1 );
        Method mvUp = Elevator.class.getMethod("moveUp");
        Method mvDown = Elevator.class.getMethod("moveDown");
//        Method dirc = Elevator.class.getMethod("direction");
        Method newDest = Elevator.class.getMethod("addNewDestinatoin", Integer.class);
        Method sts = Elevator.class.getMethod("status");
        Method currFloor = Elevator.class.getMethod("currentFloor");
        Method nextDest = Elevator.class.getMethod("nextDestionation");

        //Input methods
        MethodInput destFive = driver.addInput("DEST5", newDest, 5);
        MethodInput destFour = driver.addInput("DEST4", newDest, 4);
        MethodInput destThree = driver.addInput("DEST3", newDest, 3);
        MethodInput destTwo = driver.addInput("DEST2", newDest, 2);
        MethodInput destOne = driver.addInput("DEST1", newDest, 1);

        MethodInput up = driver.addInput("UP", mvUp);
        MethodInput down = driver.addInput("DOWN", mvDown);
//        MethodInput direction = driver.addInput("DIR", dirc);
        MethodInput status = driver.addInput("STATUS", sts);
        MethodInput floor = driver.addInput("FLOOR", currFloor);
        MethodInput nDestination = driver.addInput("NDEST", nextDest);

        StatisticSUL<MethodInput, MethodOutput> statisticSUL = new ResetCounterSUL<>("membership queries", driver);
        SUL<MethodInput, MethodOutput> effectiveSUL = statisticSUL;

        // cache
        effectiveSUL = SULCaches.createCache(driver.getInputs(), effectiveSUL);

        SULOracle<MethodInput, MethodOutput> mqOracle = new SULOracle<>(effectiveSUL);

        // create initial set of suffixes
        List<Word<MethodInput>> suffixes = new ArrayList<>();
//        suffixes.add(Word.fromSymbols(destFive));
//        suffixes.add(Word.fromSymbols(destFour));
//        suffixes.add(Word.fromSymbols(destThree));
//        suffixes.add(Word.fromSymbols(destTwo));
//        suffixes.add(Word.fromSymbols(destOne));
        suffixes.add(Word.fromSymbols(floor));
        suffixes.add(Word.fromSymbols(up));
        suffixes.add(Word.fromSymbols(down));
//        suffixes.add(Word.fromSymbols(direction));
        suffixes.add(Word.fromSymbols(status));
        suffixes.add(Word.fromSymbols(nDestination));

        // construct L* instance
        LearningAlgorithm.MealyLearner<MethodInput, MethodOutput> lstar = new ExtensibleLStarMealyBuilder<MethodInput, MethodOutput>().withAlphabet(driver.getInputs())
                .withOracle(mqOracle)
                .withInitialSuffixes(suffixes)
                .create();

        EquivalenceOracle.MealyEquivalenceOracle<MethodInput, MethodOutput> randomWalks =
                new RandomWalkEQOracle<>(driver, // system under learning
                        RESET_PROBABILITY, // reset SUL w/ this probability before a step
                        MAX_STEPS, // max steps (overall)
                        false, // reset step count after counterexample
                        new Random(RANDOM_SEED) // make results reproducible
                );

        Experiment.MealyExperiment<MethodInput, MethodOutput> experiment =
                new Experiment.MealyExperiment<>(lstar, randomWalks, driver.getInputs());

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, MethodInput, ?, MethodOutput> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(statisticSUL.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + driver.getInputs().size());

        // show model
        System.out.println();
        System.out.println("Model: ");

        GraphDOT.write(result, driver.getInputs(), System.out); // may throw IOException!
        Visualization.visualize(result, driver.getInputs());

        System.out.println("-------------------------------------------------------");
    }


    public static CompactMealy<String, String> learnElevatorControlSystem(Constructor<?> elevConstructor, Object elevParam1, Object elevParam2) throws Exception{
        SimplePOJOTestDriver driver = new SimplePOJOTestDriver(elevConstructor, elevParam1, elevParam2);
        driver.addInput("pick_1", "pickUp", 1);
        driver.addInput("pick_2", "pickUp", 2);
        driver.addInput("pick_3", "pickUp", 3);
        driver.addInput("pick_4", "pickUp", 4);
        driver.addInput("pick_5", "pickUp", 5);
        driver.addInput("step", "step");

        Alphabet<MethodInput> alphabet = driver.getInputs();

        StringMapper<MethodInput> mapper = new StringMapper<>(alphabet);
        SUL<String,String> mappedSUL = SULMappers.apply(mapper, driver);

        MembershipOracle.MealyMembershipOracle<String, String> oracle = new SULOracle<>(mappedSUL);
        MembershipOracle.MealyMembershipOracle<String, String> effOracle = oracle;
        ExtensibleLStarMealy<String, String> learner
                = new ExtensibleLStarMealyBuilder<String,String>()
                .withAlphabet(mapper.getMappedInputs())
                .withOracle(effOracle)
                .create();

        learner.startLearning();
        EquivalenceOracle.MealyEquivalenceOracle<String, String> eqOracle
                = new MealyRandomWordsEQOracle<>(
                oracle, 5, 20, 1000, new Random());
        DefaultQuery<String, Word<String>> ce;
        while ((ce = eqOracle.findCounterExample(learner.getHypothesisModel(),
                mapper.getMappedInputs())) != null) {
            System.out.println("Counterexample: " + ce);
            learner.refineHypothesis(ce);
        }

        // Return the final hypothesis
        return learner.getHypothesisModel();
    }


    public static void main(String[] args) throws Exception {
        int select = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter experiment number:");
        select = Integer.parseInt(br.readLine());
        if(select != 0){
            Class c = Class.forName("institute.teias.ds.elevator.Elevator");
            Class[] parameterType = new Class[1];
            parameterType[0] = Integer.class;
            Constructor<Elevator> cons = c.getDeclaredConstructor(parameterType);
            learnElevator(cons, 1);
        }
        else{
            Class c = Class.forName("institute.teias.ds.elevator.ElevatorControlSystem");
            Class[] parameterType = new Class[2];
            parameterType[0] = Integer.class;
            parameterType[1] = Integer.class;
            Constructor<ElevatorControlSystem> cons = c.getDeclaredConstructor(parameterType);
            CompactMealy<String,String> elevCS1Model = learnElevatorControlSystem(cons, 1,5);
            File modelFile = new File(learnedModelFile);
            if (!modelFile.exists()){
                modelFile.createNewFile();
            }
            saveMealyMachineAsDot(elevCS1Model,modelFile);
        }

//        Visualization.visualize(elevCS1Model, elevCS1Model.getInputAlphabet(), true);


    }
}