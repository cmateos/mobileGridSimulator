package edu.isistan.profiler.tosim;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BatteryEvent {
	
	private long time;
	private long estimatedTime;
	private int charge;
	
	public static List<BatteryEvent> getEvents(List<String> data){
		List<BatteryEvent> bel=new ArrayList<BatteryEvent>();
		//get firstEvent
		int count=0;
		for(String st:data){
			if(!st.contains("CPU"))count++;
			if(count==2){
				Main.firstMoment=Long.parseLong(st.substring(0, st.indexOf(",")))-2;
				break;
			}
		}
		long time=0;
		List<Long> uptime=new ArrayList<Long>();
		int charge=0;
		for(String string:data){
			if(!string.contains("CPU")){
				StringTokenizer st=new StringTokenizer(string,",");
				long newTime=Long.parseLong(st.nextToken());
				st.nextToken();st.nextToken();
				int newCharge=Integer.parseInt(st.nextToken());
				if (newCharge==charge) continue;
				if(Main.firstMoment<newTime){
					long cTime=newTime-Main.firstMoment;
					long eut=(long) ((-newCharge)/(((double)newCharge-(double)charge)/((double)newTime-(double)time)));
					uptime.add(cTime+eut);
					bel.add(new BatteryEvent(cTime, (average(uptime)-cTime)/1000, newCharge));
				}
				time=newTime;
				charge=newCharge;
			}
		}
		return bel;
	}
	
	private static long average(List<Long> data){
		long result=0;
		long cant=0;
		for(Long l:data){
			result+=l;
			cant++;
		}
		return result/cant;
	}
	
	public BatteryEvent(long time, long estimatedTime, int charge) {
		super();
		this.time = time;
		this.estimatedTime = estimatedTime;
		this.charge = charge;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(long estimatedTime) {
		this.estimatedTime = estimatedTime;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	@Override
	public String toString() {
		return "NEW_BATTERY_STATE_NODE;"+time+";"+estimatedTime+";"+charge+"00000";
	}
	
	
}
