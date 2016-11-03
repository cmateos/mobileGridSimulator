package edu.isistan.mobileGrid.node;

import java.util.HashMap;
import java.util.Map;

import edu.isistan.mobileGrid.jobs.Job;
import edu.isistan.mobileGrid.jobs.JobStatsUtils;
import edu.isistan.mobileGrid.network.NetworkModel;
import edu.isistan.mobileGrid.network.NetworkModel.Message;
import edu.isistan.mobileGrid.network.Node;
import edu.isistan.mobileGrid.network.UpdateMsg;
import edu.isistan.simulator.Entity;
import edu.isistan.simulator.Event;
import edu.isistan.simulator.Logger;
import edu.isistan.simulator.Simulation;

public class Device extends Entity implements Node, DeviceListener {
	
	public static final int EVENT_TYPE_BATTERY_UPDATE = 0;
	public static final int EVENT_TYPE_CPU_UPDATE = 1;
	public static final int EVENT_TYPE_FINISH_JOB = 2;
	public static final int EVENT_TYPE_DEVICE_START = 3;

	protected Map<Integer, Job> jobsBeingTransfered = new HashMap<Integer, Job>();
	protected Map<Integer, Long> transferStarTime = new HashMap<Integer, Long>();
	protected Map<Integer, Node> sendingTo = new HashMap<Integer, Node>();
	protected BatteryManager batteryManager;
	protected ExecutionManager executionManager;
	protected NetworkEnergyManager	networkEnergyManager;	
	
	protected int lastBatteryLevelUpdate;
	
	/**when this flag is true the device informs its State Of Charge every time it decreases
	 * in at least one percentage w.r.t the last SOC informed*/
	private boolean informSOC = true;	
	
	public Device(String name, BatteryManager bt, ExecutionManager em, NetworkEnergyManager nem) {
		super(name);
		this.batteryManager=bt;
		this.executionManager=em;
		this.networkEnergyManager = nem;		
	}

	@Override
	public void receive(Node scr, int id, Object data) {		
		if (data instanceof Job){							
			Job job = (Job)data;
			Message msj = NetworkModel.getModel().new Message(id,scr,this,data);
			msj.setAttribute(Message.SIZE, String.valueOf(job.getInputSize()));
			if (networkEnergyManager.onReceieveData(msj)){			
				JobStatsUtils.setJobTransferCompleted(job,this);
				this.executionManager.addJob(job);
			}
			else
				Logger.logEntity(this, "Failed to receive job "+job.getJobId());
		}		
	}	
	

	@Override
	public void success(int id) {				
		Message msj = NetworkModel.getModel().new Message(id,null,this,null);
		msj.setAttribute(Message.SIZE, String.valueOf(NetworkModel.getModel().getAckMessageSizeInBytes()));
		if (networkEnergyManager.onReceieveData(msj)){//if the ack could be processed then the node update its internal data structures
			this.jobsBeingTransfered.remove(id);
			this.transferStarTime.remove(id);
			this.sendingTo.remove(id);
		}
	}

	@Override
	public void fail(int id) {
		Job j=this.jobsBeingTransfered.remove(id);
		if (j!=null){
			this.sendingTo.remove(id);
			long jobStartTransferTime = this.transferStarTime.remove(id);
			//long time=Simulation.getTime()-jobStartTransferTime;			
			//JobStatsUtils.fail(j);			
			//Logger.logEntity(this, "link failed when send job result.","jobId="+j.getJobId());
			//JobStatsUtils.changeLastTransferTime(j, time,jobStartTransferTime);
		}
		
	}

	@Override
	public boolean isOnline() {
		return this.isActive();
	}

	@Override
	public void processEvent(Event e) {
		switch (e.getEventType()) {
		case Device.EVENT_TYPE_BATTERY_UPDATE:			
			int newBatteryLevel = (Integer)e.getData();
			this.batteryManager.onBatteryEvent(newBatteryLevel);
			
			if(informSOC && lastBatteryLevelUpdate-newBatteryLevel >= BatteryManager.PROFILE_ONE_PERCENT_REPRESENTATION && newBatteryLevel > 0){
				UpdateMsg updateMsg = new UpdateMsg(this.getName(), newBatteryLevel, Simulation.getTime());
				Message msg = NetworkModel.getModel().new Message(1,this,SchedulerProxy.PROXY,updateMsg);
				msg.setAttribute(Message.SIZE, String.valueOf(UpdateMsg.BATTERY_UPDATE_MSG_SIZE_IN_BYTES));
				if (this.networkEnergyManager.onSendData(msg)){
					JobStatsUtils.registerUpdateMessage(this, updateMsg);
					NetworkModel.getModel().send(this, SchedulerProxy.PROXY, 1, UpdateMsg.BATTERY_UPDATE_MSG_SIZE_IN_BYTES, updateMsg);
				}				
			}			
			break;
		case Device.EVENT_TYPE_CPU_UPDATE:
			this.executionManager.onCPUEvent((Double)e.getData());
			break;
		case Device.EVENT_TYPE_FINISH_JOB:
			Job job = (Job)e.getData();
			this.executionManager.onFinishJob(job);			
						
			Message msg = NetworkModel.getModel().new Message(1,this,SchedulerProxy.PROXY,job);
			msg.setAttribute(Message.SIZE, String.valueOf(job.getOutputSize()));
			if (this.networkEnergyManager.onSendData(msg)){//return true if energy is enough to send the message
				JobStatsUtils.transferResults(job, SchedulerProxy.PROXY, Simulation.getTime());
				//JobStatsUtils.transferBackInitiated(job);
				NetworkModel.getModel().send(this, SchedulerProxy.PROXY, 1, job.getOutputSize(), job);
				
			}
			else
				Logger.logEntity(this, "failed to send job result.","jobId="+job.getJobId());
			break;
		case Device.EVENT_TYPE_DEVICE_START:
			this.batteryManager.startWorking();
			JobStatsUtils.deviceJoinTopology(this,this.batteryManager.getStartTime());
			this.lastBatteryLevelUpdate = getBatteryLevel();
			break;
		}
		
	}
	/**
	 * Call when the device runs out of battery
	 */
	public void onBatteryDepletion(){
		JobStatsUtils.deviceLeftTopology(this, Simulation.getTime());		
		this.executionManager.shutdown();
		this.batteryManager.shutdown();		
		this.setActive(false);
		for(Integer id:this.sendingTo.keySet()){
			Node n =  this.sendingTo.get(id);
			if( n instanceof DeviceListener )
				((DeviceListener)n).onDeviceFail(this);
		}
	}
	/**
	 * Returns all the jobs assigned to the device
	 * @return
	 */
	public int getNumberOfJobs(){
		return this.executionManager.getNumberOfJobs();
	}
	/**
	 * Returns waiting jobs on this device
	 * @return
	 */
	public int getWaitingJobs(){
		return this.executionManager.getJobQueueSize();
	}
	
	public long getMIPS(){
		return this.executionManager.getMIPS();
	}
	
	public double getCPUUsage(){
		return this.executionManager.getCPUUsage();
	}
	
	public int getBatteryLevel(){
		return this.batteryManager.getCurrentBattery();
	}
	
	public long getEstimatedUptime(){
		return this.batteryManager.getEstimatedUptime();
	}
	
	public long getTotalBatteryCapacityInJoules(){
		return this.batteryManager.getBatteryCapacityInJoules();
	}

	@Override
	public void incomingData(Node scr, int id) {				
		//TODO:provide an energy-aware treatment for an incomming data message. For example, enable the wifi to be able receive data.
	}
	
	@Override
	public void failReception(Node scr, int id){		
		//TODO:provide an energy-aware treatment for a reception failure message. For example, disable the wifi.
	}
	
	public Job removeJob(int index){
		Job j=this.executionManager.getJob(index);
		this.executionManager.removeJob(index);
		return j;
	}

	@Override
	public void startTransfer(Node dst, int id, Object data) {		
		if (data instanceof Job){		
			this.jobsBeingTransfered.put(id,(Job)data);
			this.transferStarTime.put(id, Simulation.getTime());
			this.sendingTo.put(id, dst);
		}		
	}	
	

	@Override
	public void onDeviceFail(Node e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean runsOnBattery() {		
		return true;
	}

	public int getLastBatteryLevelUpdate() {
		return lastBatteryLevelUpdate;
	}

	public void setLastBatteryLevelUpdate(int lastBatteryLevelUpdate) {
		this.lastBatteryLevelUpdate = lastBatteryLevelUpdate;
	}
	
	/**returns the available Joules of the device based on the value of the last reported SOC*/
	public double getJoulesBasedOnLastReportedSOC(){
		return ((double)((this.getLastBatteryLevelUpdate() / BatteryManager.PROFILE_ONE_PERCENT_REPRESENTATION) * this.getTotalBatteryCapacityInJoules())) / (double)(100);
	}
	
	/**This method returns the last Wifi Received Signal Strength reported by the device*/
	public short getWifiRSSI(){
		return networkEnergyManager.getWifiRSSI();		
	}
	
	/**this method returns the energy (in Joules) that the device is supposed to waste when sending the
	 * amount of data indicated as argument. Data is expressed in bytes.
	 * */
	public double getEnergyWasteInTransferingData(double data){
		return networkEnergyManager.getJoulesWastedWhenTransferData(data);
	}
		
	public double getEnergyPercentageWastedInNetworkActivity(){		 
		double initialJoules = ((double)((double) getInitialSOC() / (double)BatteryManager.PROFILE_ONE_PERCENT_REPRESENTATION) * (double)batteryManager.getBatteryCapacityInJoules()) / 100;
		
		return (networkEnergyManager.getAccEnergyInTransfering() * 100) / initialJoules;
	}

	/**Returns the state of charge of the device when it joint the grid*/
	public int getInitialSOC() {
		return batteryManager.getInitialSOC();
	}
	
	/**Returns the Joules of the device when it joint the grid*/
	public double getInitialJoules(){
		return getInitialSOC() * getTotalBatteryCapacityInJoules() / ((double)100 *  (double)BatteryManager.PROFILE_ONE_PERCENT_REPRESENTATION);
	}

}
