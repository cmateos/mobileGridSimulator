package edu.isistan.proxy.jobstealing;

import java.util.Collection;
import java.util.Iterator;

import edu.isistan.mobileGrid.node.Device;
import edu.isistan.node.comparator.DeviceComparator;

public class WRAStrategy implements StealingStrategy {

	@Override
	public Device getVictim(StealerProxy sp, Device stealer) {
		Collection<Device> d=sp.getDevices();
		if(d.size()==0) return null;
		DeviceComparator cp=sp.getDevComp();
		
		Iterator<Device> iterator=d.iterator();
		Device current = iterator.next();
		
		for(Device next:d)
			if((cp.compare(current,next)>0)&&(next.getWaitingJobs()>0)
					&&(next!=stealer))
				current=next;
		if(current.getWaitingJobs()==0) return null;
		if(current==stealer) return null;
		return current;
	}

}
