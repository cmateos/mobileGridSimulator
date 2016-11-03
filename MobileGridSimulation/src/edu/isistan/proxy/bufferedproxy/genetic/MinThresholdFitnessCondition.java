package edu.isistan.proxy.bufferedproxy.genetic;

import java.util.ArrayList;
import java.util.Iterator;

/**Through this termination condition the population evolve until all individuals reach at least the minThresholdFitness**/
public class MinThresholdFitnessCondition implements TerminationCondition {

	protected double thresholdFitness;
	protected AssignmentFitnessFunction fitnessFunction;
	
	public MinThresholdFitnessCondition(Double minThreshold, AssignmentFitnessFunction assignmentFitnessFunction){
		this.thresholdFitness = minThreshold;
		fitnessFunction = assignmentFitnessFunction;
	}
		
	@Override
	public boolean satisfiedCondition(ArrayList<Short[]> population) {
		for (Iterator<Short[]> iterator = population.iterator(); iterator.hasNext();) {
			Short[] individual =  iterator.next();
			double fitness = fitnessFunction.evaluate(individual); 
			if (fitness < this.thresholdFitness)
				return false;
			else{
				System.out.println("An individual has overpassed the threshold fitness: " + fitness);
			}
		}
		return true;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName()+" threshold="+thresholdFitness;
	}

}
