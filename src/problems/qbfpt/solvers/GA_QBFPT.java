package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import problems.qbf.solvers.GA_QBF;
import problems.qbfpt.QBFPT;
import solutions.Solution;


/**
 * Metaheuristic GA (Genetic Algorithm) for obtaining an optimal solution 
 * to a QBFPT (Quadractive Binary Function  with Prohibited Triples-- {@link QBFPT}). 
 *
 * @author rsaraiva, sipamplona, vferrari
 */
public class GA_QBFPT extends GA_QBF {
	
    /**
     * The set T of prohibited triples.
     */
    private final Set<List<Integer>> T;

	/**
	 * Constructor for the GA_QBF class. The QBF objective function is passed as
	 * argument for the superclass constructor.
	 * 
	 * @param generations
	 *            Maximum number of generations.
	 * @param popSize
	 *            Size of the population.
	 * @param mutationRate
	 *            The mutation rate.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             Necessary for I/O operations.
	 */
	public GA_QBFPT(Integer generations, 
			        Integer popSize, 
			        Double mutationRate, 
			        String filename,
			        populationReplacement popMethod,
				    Boolean divMaintenance) throws IOException {
		super(generations, popSize, mutationRate, filename, popMethod, divMaintenance);
		
        // Instantiate QBFPT problem, store T and update objective reference.
        QBFPT qbfpt = new QBFPT(filename);
        T = qbfpt.getT();
        ObjFunction = qbfpt;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Adds penalty if solution is infeasible.
	 */
	@Override
	protected Double fitness(Chromosome chromosome) {
		Solution<Integer> sol = decode(chromosome);
				
		return sol.cost - (isSolutionFeasible(sol) ? 0 : 1000000);
	}
	
	/**
	 * Check if any triple restriction is violated.
	 */
    private boolean isSolutionFeasible(Solution<Integer> sol) {
    	boolean feasible = true;
        Integer e1, e2, e3;
                
    	for (List<Integer> t : T) {

            /**
             * Detach elements from (e1, e2, e3). They are stored as numbers 
             * from [0, n-1] in sol., different than in T ([1, n]).
             */
            e1 = t.get(0) - 1;
            e2 = t.get(1) - 1;
            e3 = t.get(2) - 1;

            // e1, e2 and e3 in solution -> infeasible.
            if (sol.contains(e1) && sol.contains(e2) && sol.contains(e3)) {
            	feasible = false;
            	break;
            }
        }
    	
    	return feasible;
    }
	
	/**
     * A main method used for testing the GA metaheuristic.
     */
	public static void main(String[] args) throws IOException {

		ArrayList<String> instances = new ArrayList<String>(Arrays.asList("020", "040", "060", "080", "100", "200", "400"));
		for (String instance : instances) {
			long startTime = System.currentTimeMillis();

			/* With Steady State */
			GA_QBFPT ga = new GA_QBFPT(1000, 100, 1.0 / 100.0, "instances/qbf" + instance, populationReplacement.STSTATE, true);

			/* With Diversity Maintenance */
			Solution<Integer> bestSol = ga.solve(1800.0);

			System.out.println("maxVal = " + bestSol);

			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
		}
	}
}
