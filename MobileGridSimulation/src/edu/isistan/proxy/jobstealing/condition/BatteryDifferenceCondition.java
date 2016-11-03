package edu.isistan.proxy.jobstealing.condition;

import edu.isistan.mobileGrid.node.Device;
import edu.isistan.proxy.jobstealing.StealerProxy;

public class BatteryDifferenceCondition implements StealingCondition {

	private double difference=2;
	@Override
	public boolean canSteal(Device stealer, Device victim, StealerProxy proxy) {
		return stealer.getLastBatteryLevelUpdate()>difference*victim.getLastBatteryLevelUpdate();
	}
	
	public void setDifference(String s){
		this.difference = Double.parseDouble(s);
	}

}