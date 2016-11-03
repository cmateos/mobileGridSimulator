package edu.isistan.proxy.bufferedproxy.genetic;

import edu.isistan.mobileGrid.jobs.JobStatsUtils;

public class GAExecInformation {
	
	/** information taken from GA configuration file*/
	private int 	jobs_count;
	private int 	nodes_count;
	private int 	population_size;
	private int		mutation_operations;
	private int 	crossover_operations;
	private String 	termination_condition;
	private String 	parent_selection;
	private String 	crossover_operator;
	private String 	mutation_operator;
	private String 	population_replacement;
	private boolean including_known_individual;
	
	/**runtime information*/
	private long 	elapsed_time;
	private int 	evolution_cycles;
	private double	individualBestFitness;
	private Short[] bestIndividual;
	
	public GAExecInformation(){
		this.crossover_operations=0;
		this.mutation_operations=0;
	}

	public int getJobs_count() {
		return jobs_count;
	}

	public void setJobs_count(int jobs_count) {
		this.jobs_count = jobs_count;
	}

	public int getNodes_count() {
		return nodes_count;
	}

	public void setNodes_count(int nodes_count) {
		this.nodes_count = nodes_count;
	}

	public int getPopulation_size() {
		return population_size;
	}

	public void setPopulation_size(int population_size) {
		this.population_size = population_size;
	}

	public String getTermination_condition() {
		return termination_condition;
	}

	public void setTermination_condition(String termination_condition) {
		this.termination_condition = termination_condition;
	}

	public String getParent_selection() {
		return parent_selection;
	}

	public void setParent_selection(String parent_selection) {
		this.parent_selection = parent_selection;
	}

	public String getCrossover_operator() {
		return crossover_operator;
	}

	public void setCrossover_operator(String crossover_operator) {
		this.crossover_operator = crossover_operator;
	}

	public String getMutation_operator() {
		return mutation_operator;
	}

	public void setMutation_operator(String mutation_operator) {
		this.mutation_operator = mutation_operator;
	}

	public String getPopulation_replacement() {
		return population_replacement;
	}

	public void setPopulation_replacement(String population_replacement) {
		this.population_replacement = population_replacement;
	}

	public long getElapsed_time() {
		return elapsed_time;
	}

	public void setElapsed_time(long elapsed_time) {
		this.elapsed_time = elapsed_time;
	}

	public int getEvolution_cycles() {
		return evolution_cycles;
	}

	public void setEvolution_cycles(int evolution_cycles) {
		this.evolution_cycles = evolution_cycles;
	}

	public double getIndividualBestFitness() {
		return individualBestFitness;
	}

	public void setIndividualBestFitness(double individualBestFitness) {
		this.individualBestFitness = individualBestFitness;
	}

	public Short[] getBestIndividual() {
		return bestIndividual;
	}

	public void setBestIndividual(Short[] bestIndividual2) {
		this.bestIndividual = bestIndividual2;
	}
	
	public String printIndividual(Short[] individual) {
		String ret = "";
		for (int i = 0; i < individual.length; i++) 
			ret+=individual[i]+"\n";
		return ret;
	}
	
	public boolean isIncluding_known_individual() {
		return including_known_individual;
	}

	public void setIncluding_known_individual(boolean including_known_individual) {
		this.including_known_individual = including_known_individual;
	}

	public int getMutation_operations() {
		return mutation_operations;
	}
	
	public void addMutationOperation(int operations){
		this.mutation_operations+=operations;
	}

	public int getCrossover_operations() {
		return crossover_operations;
	}
	
	public void addCrossoverOperation(int operations){
		this.crossover_operations+=operations;
	}
	
	public String printFixedParameters(){
		return ("----------------------------\n"+
				"GA fixed parameters\n"+
				"PopulationSize="+this.population_size+"\n"+
				"ChromosomeSize(Jobs)="+this.jobs_count+"\n"+
				"AlelleCount(Nodes)="+this.nodes_count+"\n"+
				"IncludingKnownIndividualInPopulation="+this.including_known_individual+"\n"+
				"TerminationCondition="+this.termination_condition+"\n"+
				"ParentSelection="+this.parent_selection+"\n"+
				"CrossoverOperator="+this.crossover_operator+"\n"+
				"CrossoversCount="+this.crossover_operations+"\n"+
				"MutationOperator="+this.mutation_operator+"\n"+
				"MutationsCount="+this.mutation_operations+"\n"+
				"PopulationReplacement="+this.population_replacement+"\n"+
				"----------------------------\n");
	}
	
	@Override
	public String toString(){
		return ("EvolutionCycles="+this.evolution_cycles+"\n"+
				"Fitness="+this.individualBestFitness+"\n"+
				"ExecTime="+JobStatsUtils.timeToMinutes(this.elapsed_time)+"\n");
				//+ printIndividual(bestIndividual));
	}
	
}
