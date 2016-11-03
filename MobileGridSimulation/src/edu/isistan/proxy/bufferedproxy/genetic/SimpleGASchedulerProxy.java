package edu.isistan.proxy.bufferedproxy.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;

import edu.isistan.mobileGrid.jobs.Job;
import edu.isistan.mobileGrid.jobs.JobStatsUtils;
import edu.isistan.mobileGrid.network.NetworkModel;
import edu.isistan.mobileGrid.node.Device;
import edu.isistan.proxy.bufferedproxy.BufferedSchedulerProxy;
import edu.isistan.proxy.dataevaluator.DataAssignment;
import edu.isistan.proxy.dataevaluator.DescendingDataAssignmentComparator;
import edu.isistan.proxy.dataevaluator.OverflowPenaltyDataEvaluator;
import edu.isistan.proxy.dataevaluator.RemainingDataTransferingEvaluator;
import edu.isistan.simulator.Event;
import edu.isistan.simulator.Logger;
import edu.isistan.simulator.Simulation;

public class SimpleGASchedulerProxy extends BufferedSchedulerProxy {
		
	private static final int EVENT_GENETIC_ALGORITHM_ROUND_FINISHED = 2;
	
	private GAConfiguration genAlgConf;
	
	private double bufferedDataToBeShedule;
	private long accRoundtime;
	
	private ArrayList<GeneticAssignmentRound> geneticRounds;
	private int currentRound; //this index is to know which of the genetic rounds is next
	private GeneticAssignmentRound currentGAR;
		

	public SimpleGASchedulerProxy(String name, String bufferValue) {
		super(name, bufferValue);
		accRoundtime=0;
		bufferedDataToBeShedule=0;
		geneticRounds = new ArrayList<GeneticAssignmentRound>();
		currentRound = 0;
	}

	@Override
	protected void initializeDeviceAssignments() {
		super.initializeDeviceAssignments();		
		HashMap<Integer,Device> devicesId = new HashMap<Integer,Device>();
		HashMap<Device,Integer> devicesObjects = new HashMap<Device,Integer>();
		int devId=0;		
		for (Iterator<Device> iterator = devices.values().iterator();iterator.hasNext();) {
			Device d = (Device)iterator.next();			
			devicesId.put(devId, d);
			devicesObjects.put(d, devId);
			devId++;
		}
		
		AssignmentFitnessFunction.devicesId = devicesId;
		AssignmentFitnessFunction.devicesObjects = devicesObjects;						
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void processEvent(Event e){
		if (e.getEventType()==SimpleGASchedulerProxy.EVENT_GENETIC_ALGORITHM_ROUND_FINISHED){			
			scheduleJobs((ArrayList<DataAssignment>)e.getData());
			if (currentRound < geneticRounds.size()){//means that there are more assignments rounds to be performed
				currentGAR = geneticRounds.get(currentRound);
				currentRound++;
				runGeneticAlgorithm(currentGAR);
			}
		}
		else{
			super.processEvent(e);
		}
	}
	
	@Override
	protected void queueJob(Job job) {
		bufferedJobs.add(job);		
		bufferedDataToBeShedule+=(((double) job.getInputSize() + (double)job.getOutputSize()) / (double)(1024*1024)); //expressed in Mb		
	}

	@Override
	protected void assignBufferedJobs() {
		ArrayList<Job> currentBufferedJobs = new ArrayList<Job>();
		currentBufferedJobs.addAll(bufferedJobs);
		
	
		//Collections.sort(currentBufferedJobs, new DescendingAggregatedJobDataComparator());
		
		GeneticAssignmentRound gar = new GeneticAssignmentRound(currentBufferedJobs,bufferedDataToBeShedule);
		geneticRounds.add(gar);
		bufferedDataToBeShedule=0;
		
		if (Simulation.getTime() < accRoundtime){ //means that the previous genetic algorithm scheduling is pending
			return;
		}
		else{
			currentGAR = geneticRounds.get(currentRound);
			currentRound++;
			runGeneticAlgorithm(currentGAR);
		}			
	}
	
	private void runGeneticAlgorithm(GeneticAssignmentRound gar){
		
		
		long startTime = System.currentTimeMillis();
		gar.setAssignmentStartTime(Simulation.getTime());
		int populationSize = genAlgConf.getPopulationSize();
		
		AssignmentFitnessFunction fitnessEvaluator = genAlgConf.getFitnessEvaluator();
		fitnessEvaluator.setGeneticAssignmentDataRound(gar);
		GAExecInformation gaInfo = genAlgConf.getGAExecInformation();
		gar.setGaInfo(gaInfo);
		
		ArrayList<Short[]> population = createFirstGeneration(gar.getJobsToSchedule());		
		
		TerminationCondition tm = genAlgConf.getTerminationCondition();
		ParentSelector parentSelectorOperator = genAlgConf.getParentSelectorOperator();
		CrossoverOperator crossoverOperator = genAlgConf.getCrossoverOperator();
		MutationOperator mutationOperator = genAlgConf.getMutationOperator();
		PopulationReplacementStrategy popReplacementStrategy = genAlgConf.getPopReplacementStrategy();
		EnumeratedIntegerDistribution shouldRecombine = new EnumeratedIntegerDistribution(new int[]{0,1}, new double[]{1-crossoverOperator.getCrossOverRate(),crossoverOperator.getCrossOverRate()});
		EnumeratedIntegerDistribution shouldMutate = new EnumeratedIntegerDistribution(new int[]{0,1}, new double[]{1-mutationOperator.getMutationRate(),mutationOperator.getMutationRate()});
				
		
		try {
			int cycles = 0;
			
			int pairCount = populationSize / 2;
			boolean oddIndividuals = false;
			if ((populationSize%2) == 1){
				pairCount++;
				oddIndividuals = true;
			}
			((IncreasedFitnessTimeCondition)tm).startEvolution();
			
			while (!tm.satisfiedCondition(population)){				
				cycles++;
				parentSelectorOperator.setPopulation(population);
				 
				ArrayList<Short[]> nextPopGeneration = new ArrayList<Short[]>();
				
				for (int pair = 0; pair < pairCount; pair++){
					ArrayList<Short[]> offspring = new ArrayList<Short[]>();
					Short[] parent1 = parentSelectorOperator.getParent();
					offspring.add(parent1);
					Short[] parent2 = null;
					if(!oddIndividuals || pair < pairCount-1){
						parent2 = parentSelectorOperator.getParent();
						offspring.add(parent2);
					}
					
					if (parent2!=null && shouldRecombine.sample()==1){
						gaInfo.addCrossoverOperation(1);
						offspring = crossoverOperator.recombine(parent1, parent2);
						
						ArrayList<Short[]> parentAndChildren = new ArrayList<Short[]>();
						parentAndChildren.add(parent1);
						parentAndChildren.add(parent2);
						parentAndChildren.addAll(offspring);						
						offspring = popReplacementStrategy.filterBestIndividuals(parentAndChildren);
					}
					
					
					ArrayList<Short[]> mutatedOffspring = new ArrayList<Short[]>(); 
					for(Iterator<Short[]> offspringIterator =  offspring.iterator(); offspringIterator.hasNext();){							
						Short[] individual = offspringIterator.next();
						if (shouldMutate.sample()==1){
							gaInfo.addMutationOperation(1);
							mutatedOffspring.add(mutationOperator.mutate(individual));
						}
						else
							mutatedOffspring.add(individual);
					}		
					offspring = mutatedOffspring;
					nextPopGeneration.addAll(offspring);
				}
				
				if (nextPopGeneration.size()==populationSize){					
					population = nextPopGeneration;	
					fitnessEvaluator.refreshCachedAssignments(population);
				}
				else{
					throw new Exception("population size is not maintained");
				}
					
			}
			gaInfo.setElapsed_time(System.currentTimeMillis()-startTime);
			gaInfo.setEvolution_cycles(cycles);			
			Short[] bestIndividual = fitnessEvaluator.getBestIndividual();
			
			
			/**Uncomment for testing purposes: load an already obtained solution*/
			//SolutionFileReader sfr = new SolutionFileReader("sim_input/assignment5000.exp", 5000);
			//Short[] bestIndividual = sfr.loadSolution();
								
			
			gaInfo.setBestIndividual(bestIndividual);
			gaInfo.setIndividualBestFitness(fitnessEvaluator.evaluate(bestIndividual));
 
			accRoundtime += Simulation.getTime()+gaInfo.getElapsed_time();
			handleRejectedJobs(bestIndividual,gar,accRoundtime);
			
			
			ArrayList<DataAssignment> solution = fitnessEvaluator.mapIndividualToSolution(bestIndividual);
			currentGAR.setAssignmentFinishedTime(accRoundtime);
			currentGAR.setAssignment(solution);
			Event roundFinishedEvent = Event.createEvent(Event.NO_SOURCE, accRoundtime, Simulation.getEntityId(this.getName()), SimpleGASchedulerProxy.EVENT_GENETIC_ALGORITHM_ROUND_FINISHED, solution);			
			Simulation.addEvent(roundFinishedEvent);
			//Uncomment the next line for test purposes: print best individual, save solution in a text file to force these assignments in a new launch
			//System.out.println(gaInfo.printIndividual(gaInfo.getBestIndividual()));
			System.out.println(gaInfo.toString());
			
		} catch (Exception e) {			
			e.printStackTrace();
		}		
	}
	

	private void handleRejectedJobs(Short[] bestIndividual, GeneticAssignmentRound gar, long rejectTime) {
		for (int i=0; i < bestIndividual.length; i++){
			if (bestIndividual[i]==-1){
				Job j = gar.getJob(i);
				JobStatsUtils.rejectJob(j,rejectTime);
				Logger.logEntity(this, "Job rejected = "+j.getJobId()+ " at "+rejectTime+ " simulation time");
			}
		}
		
	}

	private ArrayList<Short[]> createFirstGeneration(ArrayList<Job> jobsToSchedule) {
		ArrayList<Short[]> population =	generateRandomPopulation(genAlgConf.getPopulationSize(), genAlgConf.getGenesAmount(), genAlgConf.getGeneMaxValue());
		
		if (genAlgConf.isIncludeKnownIndividualInFirstGeneration()){
			Short[] bestKnownIndividual = getKnownIndividual(jobsToSchedule);
			population.remove(0);
			population.add(0, bestKnownIndividual);
		}
		
		//Also include individual that represent none assignments
		//population.remove(1);
		//population.add(1,getTheEmptyIndividual());
		
		genAlgConf.getFitnessEvaluator().clearCachedAssignments();
		DataAssignment.evaluator = new OverflowPenaltyDataEvaluator(genAlgConf.getFitnessEvaluator().getMaxEnergyAllowedForDataTransfer());
		//evaluate each individual so that the best individual of the population could be extracted when evaluate termination condition
		for (Iterator<Short[]> iterator = population.iterator(); iterator.hasNext();) {
			Short[] individual = (Short[]) iterator.next();
			genAlgConf.getFitnessEvaluator().evaluate(individual);
		}
		
		return population;
	}

	private Short[] getTheEmptyIndividual() {		
		Short[] noneAssignmentIndividual = new Short[bufferedJobs.size()];
		Arrays.fill(noneAssignmentIndividual, (short)-1);
		return noneAssignmentIndividual;
	}

	private Short[] getKnownIndividual(ArrayList<Job> jobsToSchedule) {
		
		DataAssignment.evaluator = new RemainingDataTransferingEvaluator();
		Comparator<DataAssignment> comp = new DescendingDataAssignmentComparator(DataAssignment.evaluator);
		Collections.sort(totalDataPerDevice, comp);
		
		Short[] bestIndividual = new Short[jobsToSchedule.size()];
		Arrays.fill(bestIndividual, (short)-1);
		
		for (int job = 0; job < jobsToSchedule.size(); job++){			
			int assignment=FIRST;
			while (assignment < totalDataPerDevice.size() && DataAssignment.evaluator.eval(totalDataPerDevice.get(assignment)) <= 0)
				assignment++;
			if(assignment < totalDataPerDevice.size()){
				totalDataPerDevice.get(assignment).scheduleJob(jobsToSchedule.get(job));
				bestIndividual[job] = genAlgConf.getFitnessEvaluator().getDeviceId(totalDataPerDevice.get(assignment).getDevice());
				Collections.sort(totalDataPerDevice, comp);
			}
			else
				break;
		}
		
		genAlgConf.getFitnessEvaluator().evaluate(bestIndividual);
		deviceToAssignmentsMap.clear();
		return bestIndividual;
	}
	
	

	protected void scheduleJobs(ArrayList<DataAssignment> solution) {		
		
		for (Iterator<DataAssignment> iterator = solution.iterator(); iterator.hasNext();) {
			DataAssignment deviceAssignment = (DataAssignment) iterator.next();
			Device current = deviceAssignment.getDevice();			
			for (Iterator<Job> iterator2 = deviceAssignment.getAssignedJobs().iterator(); iterator2.hasNext();) {
				Job job = (Job) iterator2.next();
				Logger.logEntity(this, "Job assigned to ", job.getJobId() ,current);
				long time=NetworkModel.getModel().send(this, current, idSend++,  job.getInputSize(), job);
				long currentSimTime = Simulation.getTime();
				JobStatsUtils.transfer(job, current, time-currentSimTime,currentSimTime);				
			}			
		}		
		
	}	

	private ArrayList<Short[]> generateRandomPopulation(int populationSize,	int individualChromosomesAmount, int individualAlleleMaxValue) {
		ArrayList<Short[]> randPopulation = new ArrayList<Short[]>();
		Random rand = new Random();
		
		int individualIndex = 0;
		/*if (populationSize >= individualAlleleMaxValue){
			for (; individualIndex < individualAlleleMaxValue; individualIndex++){
				Short[] individual = new Short[individualChromosomesAmount];
				Arrays.fill(individual, (short)individualIndex);
				randPopulation.add(individual);
			}
		}*/
		
		
		for (; individualIndex < populationSize; individualIndex++){
			Short[] individual =  new Short[individualChromosomesAmount];
			for (int chromosome = 0; chromosome < individualChromosomesAmount; chromosome++){
				individual[chromosome] = (short) (rand.nextInt(individualAlleleMaxValue+1)-1);
			}
			randPopulation.add(individual);
		}
		
		return randPopulation;
	}

	public GAConfiguration getGenAlgConf() {
		return genAlgConf;
	}

	public void setGenAlgConf(GAConfiguration genAlgConf) {
		this.genAlgConf = genAlgConf;
	}

	public String printGeneticRoundsInfo() {
		//GAExecInformation gaInfo = getGenAlgConf().getGAExecInformation();
		String gRInfo="";//gaInfo.printFixedParameters()+"\n";
		int i=0;
		for (Iterator<GeneticAssignmentRound> garIt=geneticRounds.iterator(); garIt.hasNext();){
			GeneticAssignmentRound gar = garIt.next();
			gRInfo+="Round:"+i+" StartTime:"+gar.getAssignmentStartTime()+" ScheduleTime:"+gar.getAssignmentFinishedTime()+"\n";
			gRInfo+=gar.getGaInfo().toString()+"\n";
			i++;
		}
		return gRInfo;
	}
	
}
