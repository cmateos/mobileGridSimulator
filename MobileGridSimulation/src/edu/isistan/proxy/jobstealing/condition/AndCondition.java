package edu.isistan.proxy.jobstealing.condition;

import edu.isistan.mobileGrid.node.Device;
import edu.isistan.proxy.jobstealing.StealerProxy;

public class AndCondition extends CombinedCondition {

	@Override
	public boolean canSteal(Device stealer, Device victim, StealerProxy proxy) {
		for(StealingCondition c:this.conditions)
			if(!c.canSteal(stealer, victim, proxy)) return false;
		return true;
	}

}
