package edu.isistan.proxy.bufferedproxy;

import java.util.HashMap;
import java.util.Iterator;

import edu.isistan.mobileGrid.jobs.Job;
import edu.isistan.mobileGrid.jobs.JobStatsUtils;
import edu.isistan.mobileGrid.network.NetworkModel;
import edu.isistan.mobileGrid.node.Device;
import edu.isistan.proxy.dataevaluator.DataAssignment;
import edu.isistan.proxy.dataevaluator.RSSIDataEvaluator;
import edu.isistan.simulator.Logger;
import edu.isistan.simulator.Simulation;

public class HungarianBasedProxy extends BufferedSchedulerProxy {
	
	private boolean usePreviousAssignmentsInformation = true;
	
	public HungarianBasedProxy(String name) {
		super(name, "1");
		DataAssignment.evaluator= new RSSIDataEvaluator();
		bufferSize=devices.size();
	}
		
	
	@Override
	protected void queueJob(Job job) {
		bufferedJobs.add(job);
	}

	@Override
	protected void assignBufferedJobs() {
		/**build the cost matrix*/
		//Columns of the costsMatrix represent the nodes and Rows represent the jobs.
		//Each cell contains the cost (in energy percentage) of transfer the 
		//(input+output) data of the job to a node. In a proxy-based infrastructure
		//it is assumed that the proxy sees all the nodes, so, all cell should be
		//filled with a value different from infinito. Infinito values will be used
		//to represent unreachable nodes.
		HashMap<Integer,Device> mapOfDevices = new HashMap<Integer,Device>();
		double[][] costsMatrix = new double[devices.size()][devices.size()];		
		//fill the matrix with the energetic cost of sending each job to each device	
				
		double costij;	
		
		for (int jobIndex = 0; jobIndex < bufferedJobs.size(); jobIndex++) {
			Job job = bufferedJobs.get(jobIndex);
			int deviceIndex=0;
			for (Iterator<Device> iterator2 = devices.values().iterator(); iterator2.hasNext();) {
				Device device = (Device) iterator2.next();
				DataAssignment deviceCurrentAssignment = deviceToAssignmentsMap.get(device);
				
				if (deviceCurrentAssignment == null){
					deviceCurrentAssignment = new DataAssignment(null);
					deviceCurrentAssignment.setDevice(device);						
				}
				DataAssignment hypotheticalDeviceAssignment = deviceCurrentAssignment.clone();
				double inputData =  (double)((double)(job.getInputSize())/1024)/1024;
				double outputData = (double)((double)(job.getOutputSize())/1024)/1024;
				if (usePreviousAssignmentsInformation){
					inputData +=  hypotheticalDeviceAssignment.getMbToBeReceived();
					outputData += hypotheticalDeviceAssignment.getMbToBeSend();
				}
				hypotheticalDeviceAssignment.setMbToBeReceived(inputData);
				hypotheticalDeviceAssignment.setMbToBeSend(outputData);
				costij = DataAssignment.evaluator.eval(hypotheticalDeviceAssignment);
				costsMatrix[jobIndex][deviceIndex] = costij;
				mapOfDevices.put(deviceIndex, device);
				deviceIndex++;
			}
		}
		
		//invoke Hungarian algorithm
		int[][] assignments = HungarianAlgorithm.hgAlgorithm(costsMatrix, "min");
		
		/////////////////////////////////////////////////////////////////////////
		//map jobs to nodes
		for (int i=0; i < assignments.length; i++){
			assignJob(bufferedJobs.get(assignments[i][1]),mapOfDevices.get(i));
		}		
	}

	private void assignJob(Job job, Device device) {		
		Logger.logEntity(this, "Job assigned to ", job.getJobId() ,device);
		long time=NetworkModel.getModel().send(this, device, idSend++,  job.getInputSize(), job);
		long currentSimTime = Simulation.getTime();
		JobStatsUtils.transfer(job, device, time-currentSimTime,currentSimTime);
		DataAssignment da = deviceToAssignmentsMap.get(device);
		da.setMbToBeReceived(da.getMbToBeReceived()+((double)((double)(job.getInputSize())/1024)/1024));
		da.setMbToBeSend(da.getMbToBeSend()+((double)((double)(job.getOutputSize())/1024)/1024));
	}
	
}
