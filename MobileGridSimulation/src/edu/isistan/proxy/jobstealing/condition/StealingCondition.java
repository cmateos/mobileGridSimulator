package edu.isistan.proxy.jobstealing.condition;

import edu.isistan.mobileGrid.node.Device;
import edu.isistan.proxy.jobstealing.StealerProxy;

public interface StealingCondition {

	public boolean canSteal(Device stealer, Device victim, StealerProxy proxy);
}
