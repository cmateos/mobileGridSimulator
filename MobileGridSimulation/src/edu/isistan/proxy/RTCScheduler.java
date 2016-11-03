package edu.isistan.proxy;

import java.util.Collections;

import edu.isistan.mobileGrid.jobs.Job;
import edu.isistan.mobileGrid.jobs.JobStatsUtils;
import edu.isistan.mobileGrid.network.NetworkModel;
import edu.isistan.mobileGrid.node.Device;
import edu.isistan.proxy.dataevaluator.DataAssignment;
import edu.isistan.proxy.dataevaluator.DescendingDataAssignmentComparator;
import edu.isistan.proxy.dataevaluator.RemainingDataTransferingEvaluator;
import edu.isistan.simulator.Logger;
import edu.isistan.simulator.Simulation;

public class RTCScheduler extends DataIntensiveScheduler {

	public RTCScheduler(String name) {
		super(name);		
	}

	@Override
	protected void assignJob(Job job) {
		
		Collections.sort(totalDataPerDevice, new DescendingDataAssignmentComparator(new RemainingDataTransferingEvaluator()));
		DataAssignment d =  totalDataPerDevice.get(FIRST);				
		d.scheduleJob(job);
		
		Device current = totalDataPerDevice.get(FIRST).getDevice();
		Logger.logEntity(this, "Job assigned to ", job.getJobId() ,current);
		long time=NetworkModel.getModel().send(this, current, idSend++,  job.getInputSize(), job);
		long currentSimTime = Simulation.getTime();
		JobStatsUtils.transfer(job, current, time-currentSimTime,currentSimTime);
		
		jobAssignments.put(job, d);
	}

}
