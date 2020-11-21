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
     * Run GA for QBFPT
     */
	public static void run(Integer generations, 
					       Integer popSize, 
					       Double mutationRate, 
					       String filename,
					       populationReplacement popMethod,
						   Boolean divMaintenance,
						   Double maxTime) 
					   throws IOException {

		long startTime = System.currentTimeMillis();

		/* With Steady State */
		GA_QBFPT ga = new GA_QBFPT(generations,
								   popSize, 
								   mutationRate,
								   filename, 
								   popMethod, 
								   divMaintenance);

		/* With Diversity Maintenance */
		Solution<Integer> bestSol = ga.solve(maxTime);

		System.out.println("maxVal = " + bestSol);

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
	}
	
	public static void testAll(Integer generations, 
						       Integer popSize, 
						       Double mutationRate, 
						       populationReplacement popMethod,
							   Boolean divMaintenance,
							   Double maxTime) 
					   throws IOException {
				
		String inst[] = {"020", "040", "060", "080", "100", "200", "400"};
		
		for(String file : inst) {
			GA_QBFPT.run(generations, popSize, mutationRate, 
						 "instances/qbf" + file, popMethod,
						 divMaintenance, maxTime);
		}
	}

	
	/**
     * A main method used for testing the GA metaheuristic.
     */
	public static void main(String args[]) throws IOException {
		
		// Fixed parameters
		Double maxTime = 1800.0;
		Integer generations = 10000;
		
		// Changeable parameters.
		Integer popSize1 = 100, popSize2 = 50; 
		Double mutationRate1 = 0.01, mutationRate2 = 0.1;
		
		// Testing
		GA_QBFPT.run(generations, popSize1, mutationRate1, 
					 "instances/qbf100",
					 populationReplacement.STSTATE,
					 true, maxTime);
		
		/*
		// 1 - Testing pop1/mut1/elitism/no div
		GA_QBFPT.testAll(generations, popSize1, mutationRate1,
						 populationReplacement.ELITE,
						 false, maxTime);
		
		// 2 - Testing pop1/mut1/ststate/no div
		GA_QBFPT.testAll(generations, popSize1, mutationRate1,
						 populationReplacement.STSTATE,
						 false, maxTime);

		// 3 - Testing pop1/mut1/ststate/div
		GA_QBFPT.testAll(generations, popSize1, mutationRate1,
						 populationReplacement.STSTATE,
						 true, maxTime);

		// 4 - Testing pop2/mut1/ststate/div
		GA_QBFPT.testAll(generations, popSize2, mutationRate1,
				 		 populationReplacement.STSTATE,
				 		 true, maxTime);
		
		// 5 - Testing pop1/mut2/ststate/div
		GA_QBFPT.testAll(generations, popSize1, mutationRate2,
				 		 populationReplacement.STSTATE,
				 		 true, maxTime);
		*/
	}
}
