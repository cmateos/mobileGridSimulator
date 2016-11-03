package edu.isistan.proxy;

import java.util.Collection;
import java.util.Iterator;

import edu.isistan.mobileGrid.jobs.Job;
import edu.isistan.mobileGrid.jobs.JobStatsUtils;
import edu.isistan.mobileGrid.network.NetworkModel;
import edu.isistan.mobileGrid.node.Device;
import edu.isistan.mobileGrid.node.SchedulerProxy;
import edu.isistan.node.comparator.DefaultSEASComparator;
import edu.isistan.node.comparator.DeviceComparator;
import edu.isistan.simulator.Event;
import edu.isistan.simulator.Logger;
import edu.isistan.simulator.Simulation;

public class GridEnergyAwareLoadbalancing extends SchedulerProxy {

	protected DeviceComparator devComp=new DefaultSEASComparator();
	protected int idSend = 0;
	
	public GridEnergyAwareLoadbalancing(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void processEvent(Event e) {
		if(EVENT_JOB_ARRIVE!=e.getEventType()) throw new IllegalArgumentException("Unexpected event");
		Job j=(Job)e.getData();
		JobStatsUtils.addJob(j, this);
		Logger.logEntity(this, "Job arrived ", j.getJobId());
		assignJob(j);
	}

	protected void assignJob(Job j) {
		Collection<Device> deviceList = this.devices.values();
		Iterator<Device> iterator=deviceList.iterator();
		if (iterator.hasNext()){
			Device current = iterator.next();
			for(Device ac: deviceList)
				if(this.devComp.compare(ac, current)>0) current=ac;
			Logger.logEntity(this, "Job assigned to ", j.getJobId() ,current);
			long time=NetworkModel.getModel().send(this, current, idSend++,  j.getInputSize(), j);
			long currentSimTime = Simulation.getTime();
			JobStatsUtils.transfer(j, current, time-currentSimTime,currentSimTime);
		}
		else{
			JobStatsUtils.rejectJob(j,Simulation.getTime());
			Logger.logEntity(this, "Job rejected = "+j.getJobId()+ " at "+Simulation.getTime()+ " simulation time");
		}
		
		
	}

	public DeviceComparator getDevComp() {
		return this.devComp;
	}

	public void setDevComp(DeviceComparator devComp) {
		this.devComp = devComp;
		Logger.logEntity(this, "Using Comparator", devComp.getClass().getName());
	}

	@Override
	public boolean runsOnBattery() {		
		return false;
	}
}
