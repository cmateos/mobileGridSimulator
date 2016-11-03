package edu.isistan.mobileGrid.network;

public class UpdateMsg {

	public static final int BATTERY_UPDATE_MSG_SIZE_IN_BYTES = 2346; //http://stackoverflow.com/questions/5543326/what-is-the-total-length-of-pure-tcp-ack-sent-over-ethernet 
	//16 bytes composed by: nodeId=4 bytes, %ofRemainingBattery=4 bytes, timeStamp=8bytes
	
	private String nodeId;
	private int percentageOfRemainingBattery;
	private long timeStamp;	
	
	
	public UpdateMsg(String nodeId, int perOfRemainingBat, long timeStamp){
		this.nodeId = nodeId;
		this.percentageOfRemainingBattery = perOfRemainingBat;
		this.timeStamp = timeStamp;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	public int getPercentageOfRemainingBattery() {
		return percentageOfRemainingBattery;
	}
	public void setPercentageOfRemainingBattery(int percentageOfRemainingBattery) {
		this.percentageOfRemainingBattery = percentageOfRemainingBattery;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}	
}
