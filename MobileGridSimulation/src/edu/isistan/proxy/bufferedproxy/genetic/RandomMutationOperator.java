package edu.isistan.proxy.bufferedproxy.genetic;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class RandomMutationOperator extends MutationOperator {

	private Random geneValueGen;
	private Random geneCountGen;
	private Random genePositionGen;
	
	public RandomMutationOperator(double mutRate, int geneMaxValue) {
		super(mutRate, geneMaxValue);
		geneValueGen = new SecureRandom();
		geneCountGen = new SecureRandom();
		genePositionGen = new SecureRandom();
	}

	@Override
	public Short[] mutate(Short[] individual) {
		int individualLength = individual.length;
		Short[] mutatedIndividual = Arrays.copyOf(individual, individualLength);
		
		int geneCount = geneCountGen.nextInt(individualLength);
		for(int gene = 0; gene < geneCount; gene++){
			int genePosition = genePositionGen.nextInt(individualLength);
			mutatedIndividual[genePosition] = (short) (geneValueGen.nextInt(geneMaxValue+1) - 1);
		}		
		
		return mutatedIndividual;
	}

}
