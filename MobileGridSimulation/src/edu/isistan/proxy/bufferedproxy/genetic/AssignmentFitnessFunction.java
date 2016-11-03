package edu.isistan.proxy.bufferedproxy.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import edu.isistan.mobileGrid.node.Device;
import edu.isistan.mobileGrid.node.SchedulerProxy;
import edu.isistan.proxy.dataevaluator.DataAssignment;
import edu.isistan.proxy.dataevaluator.OverflowPenaltyDataEvaluator;

public class AssignmentFitnessFunction {

	private HashMap<Short[],Double> cachedFitnessValues;
	public static HashMap<Integer,Device> devicesId;
	public static HashMap<Device,Integer> devicesObjects;
	public GeneticAssignmentRound gar;
	private int maxEnergyAllowedForDataTransfer = 100;		
	
	public AssignmentFitnessFunction(int maxAvailable) {
		cachedFitnessValues = new HashMap<Short[],Double>();
		this.setMaxEnergyAllowedForDataTransfer(maxAvailable);
		DataAssignment.evaluator = new OverflowPenaltyDataEvaluator(getMaxEnergyAllowedForDataTransfer()); 
		
	}	
	
	public double evaluate(Collection<DataAssignment> dataAssignments) {
						
			double gridDataTransfered = 0.0d;
			double gridJobsTransfered = 0.0d;
			double gridEnergyUsed = 0.0d;
			double gridAvailableEnergy = SchedulerProxy.PROXY.getCurrentAggregatedNodesEnergy();		
			
			double[] nodesEnergySpent = new double[devicesId.size()];
			Arrays.fill(nodesEnergySpent, 0.0);
			
			for (Iterator<DataAssignment> iterator = dataAssignments.iterator(); iterator.hasNext();) {
				DataAssignment da = (DataAssignment) iterator.next();				
				DataAssignment.evaluator.eval(da);
				gridDataTransfered += da.getAffordableDataTranfered();
				gridJobsTransfered += da.getAffordableJobCompletelyTransfered();
				double devEnergy = da.getDeviceEnergyWasted();
				gridEnergyUsed+=devEnergy;
				short devId=getDeviceId(da.getDevice());
				nodesEnergySpent[devId]+=devEnergy;
			}
			//normalized data transfered			
			gridDataTransfered = gridDataTransfered / gar.getTotalDataToBeSchedule();
			//normalized job transfered
			gridJobsTransfered = gridJobsTransfered / gar.getGenesAmount();
			//normalized energy wasted
			gridEnergyUsed /= gridAvailableEnergy;
			//deviation nodes energy spent
			//double nodesEnergySpentDeviation = evaluateAssignmentsEnergyFairness(nodesEnergySpent);
			return (gridJobsTransfered + gridDataTransfered);// - nodesEnergySpentDeviation);		
	}	


	//assignment fairness is the standard deviation of energy spend by all nodes
	private double evaluateAssignmentsEnergyFairness(double[] nodesEnergySpend) {
		
		nodesEnergySpend = transformIntoPercentages(nodesEnergySpend);
		
		double sum = 0;
		for (int i = 0; i < nodesEnergySpend.length; i++)
			sum += nodesEnergySpend[i];		
		double mean = sum / nodesEnergySpend.length;
		
		double sumOfsqrtComponents = 0;		
		for (int i = 0; i < nodesEnergySpend.length; i++)
			sumOfsqrtComponents += Math.pow(nodesEnergySpend[i]-mean, 2);
		
		return Math.sqrt(sumOfsqrtComponents / nodesEnergySpend.length);
	}


	private double[] transformIntoPercentages(double[] nodesEnergySpend) {
		double[] nodesPerSpent = new double[devicesId.size()];
		Arrays.fill(nodesPerSpent, 0.0);
		
		for (int i=0; i < nodesEnergySpend.length; i++){
			Device dev = devicesId.get(i);
			double perValue = (double)(nodesEnergySpend[i] / dev.getInitialJoules());
			nodesPerSpent[i]=perValue;
		}
		return nodesPerSpent;
	}

	/**When invoking this method, positions values of individual param are supposed to contain a node id each one and positions represent jobs. If
	 * a node value is -1, then the job is currently not assigned.*/
	public double evaluate(Short[] individual){
		if (!cachedFitnessValues.containsKey(individual)){
			HashMap<Integer,DataAssignment> deviceAssignments = convertIntoDeviceAssignments(individual);
			double fitness = evaluate(deviceAssignments.values());
			cachedFitnessValues.put(individual, fitness);
			return fitness;
		}
		else{
			return cachedFitnessValues.get(individual);
		}
	}

	private HashMap<Integer, DataAssignment> convertIntoDeviceAssignments(Short[] assignments) {
		HashMap<Integer,DataAssignment> deviceAssignments = new HashMap<Integer,DataAssignment>();		
		
		for (int job = 0; job < assignments.length; job++){
			int node_id = assignments[job];
			if (node_id != -1){
				DataAssignment nodeAssignment = null;
				if (!deviceAssignments.containsKey(node_id)){
					nodeAssignment = new DataAssignment(devicesId.get(node_id));
					deviceAssignments.put(node_id, nodeAssignment);
				}
				else
					nodeAssignment = deviceAssignments.get(node_id);
				
				nodeAssignment.scheduleJob(gar.getJob(job));
			}
		}
		return deviceAssignments;
	}
	
	public void removeCachedAssignment(Short[] assignment){
		if(cachedFitnessValues.containsKey(assignment)){
			cachedFitnessValues.remove(assignment);
		}
	}

	public void clearCachedAssignments(){
		cachedFitnessValues.clear();
	}

	public void refreshCachedAssignments(ArrayList<Short[]> population) {
		HashMap<Short[],Double> refreshedCached = new HashMap<Short[],Double> ();
		for (Iterator<Short[]> iterator = population.iterator(); iterator.hasNext();) {
			Short[] assignment = (Short[]) iterator.next();
			Double fitnessvalue = (cachedFitnessValues.containsKey(assignment))? new Double(cachedFitnessValues.get(assignment)) : this.evaluate(assignment); 
			refreshedCached.put(assignment, fitnessvalue);			
		}
		cachedFitnessValues = refreshedCached;
	}
	
	public Short[] getBestIndividual(){
		double currentGenerationBestFitness = Double.NEGATIVE_INFINITY;
		Short[] bestIndividual = null;
		for (Iterator<Short[]> iterator = cachedFitnessValues.keySet().iterator(); iterator.hasNext();) {
			Short[] individual =  iterator.next();
			double currentIndividualFitness = evaluate(individual);
			if (currentIndividualFitness > currentGenerationBestFitness){
				currentGenerationBestFitness = currentIndividualFitness;
				bestIndividual = individual;
			}
		}
		return bestIndividual;
	}
	
	public ArrayList<DataAssignment> mapIndividualToSolution(Short[] individual){
		HashMap<Integer,DataAssignment> deviceAssignments = convertIntoDeviceAssignments(individual);
		return new ArrayList<DataAssignment>(deviceAssignments.values());
	}


	public Short getDeviceId(Device device) {
		return ((Integer)devicesObjects.get(device)).shortValue();		
	}

	public void setGeneticAssignmentDataRound(GeneticAssignmentRound gar) {
		this.gar=gar;		
	}

	public int getMaxEnergyAllowedForDataTransfer() {
		return maxEnergyAllowedForDataTransfer;
	}

	public void setMaxEnergyAllowedForDataTransfer(
			int maxEnergyAllowedForDataTransfer) {
		this.maxEnergyAllowedForDataTransfer = maxEnergyAllowedForDataTransfer;
	}

}
