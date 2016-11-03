package edu.isistan.node.comparator;

import edu.isistan.mobileGrid.node.Device;

public class SEASPercentBased extends DefaultSEASComparator{

	public double getValue(Device arg0) {
		double mips=arg0.getMIPS();
		double uptime=arg0.getLastBatteryLevelUpdate();
		double nJobs=arg0.getNumberOfJobs()+1;
		return (mips*uptime)/nJobs;
	}

	
}
