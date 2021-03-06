package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import java.util.ArrayList;
import java.util.HashSet;

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
    private final ArrayList<List<Integer[]>> T;

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
     * Viabilizes chromosomes for QBFPT.
     * Checks if a triple is being violated, and removes random element from said triple.
     * @param ind Individual to be viabilized for QBFPT. 
     * @return viable individual.
     */
    private Chromosome viabilize(Chromosome ind) {
        Integer e, i;
    	
		for (i = 0; i < chromosomeSize; i++) {
			
			// If the gene is not active, ignore.
			if(ind.get(i) == 0) continue;
			
			// Check triples
			for (Integer[] t : T.get(i)) {
				
				// If the triple is active (infeasible), set random element as 0.
				if (ind.get(t[0]) == 1 && ind.get(t[1]) == 1 && ind.get(t[2]) == 1) {
					e = rng.nextInt(3);
					ind.set(t[e], 0);
				}
			}
		}
    	
    	return ind;
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see metaheuristics.ga.AbstractGA#generateRandomChromosome()
	 */
	@Override
	protected Chromosome generateRandomChromosome() {
		Chromosome chromosome = new Chromosome();
		
		// Generate
		for (int i = 0; i < chromosomeSize; i++) {
			chromosome.add(rng.nextInt(2));
		}
		
		// Viabilize and return
		return viabilize(chromosome);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Adds penalty if solution is infeasible.
	 */
	@Override
	protected Double fitness(Chromosome chromosome) {
		Solution<Integer> sol = decode(chromosome);
				
		return sol.cost; //- (isSolutionFeasible(sol) ? 0 : 1000000);
	}
	
	/**
	 * Check if any triple restriction is violated.
	 */
    private boolean isSolutionFeasible(Solution<Integer> sol) {
        boolean feasible = true;
        Set<Integer> _sol = new HashSet<Integer>(sol);
                
    	for (Integer e : sol) {
    		for(Integer[] t : T.get(e)) {
    			if(_sol.contains(t[1]) && _sol.contains(t[2])) {
    				feasible = false;
    				break;
    			}
    		}
    		if(!feasible) break;
        }
    	
    	return feasible;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Viabilizes infeasible solutions.
     */
    protected Population selectPopulation(Population original, Population offsprings) {
    	
    	// Viabilize
    	for(int i=0; i<offsprings.size(); i++) 
    		offsprings.set(i, viabilize(offsprings.get(i)));
    	
    	// Select pop.
		if(this.popMethod == populationReplacement.ELITE)
			return elitism(offsprings);
		else if(this.popMethod == populationReplacement.STSTATE)
			return steadyState(original, offsprings);
		
		return offsprings;
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
		
		GA_QBFPT ga = new GA_QBFPT(generations,
								   popSize, 
								   mutationRate,
								   filename, 
								   popMethod, 
								   divMaintenance);

		Solution<Integer> bestSol = ga.solve(maxTime);
		System.out.println("maxVal = " + bestSol);
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
	}
	
	public static void testAll(Integer generations, 
						       Integer popSize, 
						       Double mutationRateInd, 
						       populationReplacement popMethod,
							   Boolean divMaintenance,
							   Double maxTime) 
					   throws IOException {
				
		String inst[] = {"020", "040", "060", "080", "100", "200", "400"};
		Double mutationRate = mutationRateInd;
		Integer size;
		
		for(String file : inst) {
			if(mutationRateInd < 0.0) {
				size = Integer.parseInt(file);
				mutationRate = 1.0 / (float)size;
			}
				
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
		Double mutationRate1 = 0.01, mutationRate2 = -1.0;
		
//		// Testing
//		GA_QBFPT.testAll(generations, popSize2, mutationRate1,
//					 populationReplacement.STSTATE,
//					 false, maxTime);

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

		// 6 - Testing pop2/mut2/elitism/no div
		GA_QBFPT.testAll(generations, popSize2, mutationRate2,
				populationReplacement.ELITE,
				false, maxTime);

		// 7 - Testing pop2/mut2/ststate/div
		GA_QBFPT.testAll(generations, popSize2, mutationRate2,
				populationReplacement.STSTATE,
				true, maxTime);

	}
}
