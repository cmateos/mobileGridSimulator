package edu.isistan.proxy.jobstealing.condition;

import edu.isistan.mobileGrid.node.Device;
import edu.isistan.proxy.jobstealing.StealerProxy;

public class NoJobsCondition implements StealingCondition {

	@Override
	public boolean canSteal(Device stealer, Device victim, StealerProxy proxy) {
		return stealer.getWaitingJobs()==0;
	}

}
