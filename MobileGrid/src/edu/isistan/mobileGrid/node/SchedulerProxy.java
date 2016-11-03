package edu.isistan.mobileGrid.node;

import java.util.HashMap;
import java.util.Iterator;

import edu.isistan.mobileGrid.jobs.Job;
import edu.isistan.mobileGrid.jobs.JobStatsUtils;
import edu.isistan.mobileGrid.network.NetworkModel;
import edu.isistan.mobileGrid.network.Node;
import edu.isistan.mobileGrid.network.UpdateMsg;
import edu.isistan.simulator.Entity;
import edu.isistan.simulator.Event;
import edu.isistan.simulator.Logger;
import edu.isistan.simulator.Simulation;

public abstract class SchedulerProxy extends Entity  implements Node, DeviceListener{

	public static final int EVENT_JOB_ARRIVE = 1;

	public abstract void processEvent(Event e);

	public static SchedulerProxy PROXY;
	protected HashMap<String,Device> devices = new HashMap<String,Device>();

	public SchedulerProxy(String name) {
		super(name);
		PROXY=this;
		Simulation.addEntity(this);
		NetworkModel.getModel().addNewNode(this);
		Logger.logEntity(this, "Proxy created", this.getClass().getName());
	}
	
	public double getCurrentAggregatedNodesEnergy(){
		double currentAggregatedEnergy = 0;
		for (Iterator<Device> iterator = devices.values().iterator(); iterator.hasNext();) {
			Device dev = (Device) iterator.next();
			currentAggregatedEnergy+=dev.getJoulesBasedOnLastReportedSOC();
		}
		return currentAggregatedEnergy;
	}


	@Override
	public void startTransfer(Node dst, int id, Object data) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void incomingData(Node scr, int id) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void failReception(Node scr, int id){
		// TODO Auto-generated method stub
	}
	
	@Override
	public void receive(Node scr, int id, Object data) {
		if (data instanceof Job){
			Job jobResult = (Job) data;			  
			JobStatsUtils.successTrasferBack(jobResult);
			
		}else
			if(data instanceof UpdateMsg){
				UpdateMsg msg = (UpdateMsg) data;
				Device device = devices.get(msg.getNodeId());
				Logger.logEntity(this, "Battery update received from device "+msg.getNodeId()+" value="+msg.getPercentageOfRemainingBattery());
				device.setLastBatteryLevelUpdate(msg.getPercentageOfRemainingBattery());				
				JobStatsUtils.registerUpdateMessage(this,(UpdateMsg)data);				
			}
	}	
	
	@Override
	public void success(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fail(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isOnline() {
		return true;
	}

	public void remove(Device device) {
		this.devices.remove(device.getName());
	}

	public void addDevice(Device device) {
		this.devices.put(device.getName(),device);
	}

	@Override
	public void onDeviceFail(Node e) {
		// TODO Auto-generated method stub
		
	}
}