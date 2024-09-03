package edu.isistan.proxy.bufferedproxy.genetic;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class IncrementalMutationOperator extends MutationOperator {

	private Random geneCountGen;
	private Random genePositionGen;
	
	public IncrementalMutationOperator(double mutationRate, int geneMaxValue) {
		super(mutationRate,geneMaxValue);
		geneCountGen = new SecureRandom();
		genePositionGen = new SecureRandom();
	}

	@Override
	public Short[] mutate(Short[] individual) {
		
		int individualLength = individual.length;
		int geneCount = geneCountGen.nextInt(individualLength);	
		Short[] mutatedIndividual = Arrays.copyOf(individual, individualLength);
		
		for(int gene = 0; gene < geneCount; gene++){
			int genePosition = genePositionGen.nextInt(individualLength);
			short mutatedValue = (short) (mutatedIndividual[genePosition]+1 == this.geneMaxValue ? -1 : mutatedIndividual[genePosition]+1);
			mutatedIndividual[genePosition] = mutatedValue;
		}
		return mutatedIndividual;
	}

}
