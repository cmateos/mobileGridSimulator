package edu.isistan.proxy.bufferedproxy.genetic;

import java.util.ArrayList;

public abstract class PopulationReplacementStrategy {
	
	protected int numberOfBestIndividuals;
	
	public PopulationReplacementStrategy(int numberOfBestIndividuals){
		this.numberOfBestIndividuals = numberOfBestIndividuals;
	}
	
	public abstract ArrayList<Short[]> filterBestIndividuals(ArrayList<Short[]> currentPop);

}
