package edu.isistan.reader;

import edu.isistan.mobileGrid.node.BatteryManager;
import edu.isistan.mobileGrid.node.Device;
import edu.isistan.mobileGrid.node.ExecutionManager;
import edu.isistan.mobileGrid.node.NetworkEnergyManager;
import edu.isistan.node.DefaultBatteryManager;
import edu.isistan.node.DefaultExecutionManager;
import edu.isistan.node.DefaultNetworkEnergyManager;
import edu.isistan.node.jobstealing.JSDevice;
import edu.isistan.node.jobstealing.JSSEASBatteryManager;
import edu.isistan.node.jobstealing.JSSEASExecutionManager;

public class JobStealingFactory implements ManagerFactory {

	@Override
	public DefaultBatteryManager createBatteryManager(int prof, int charge,
			long estUptime,long batteryCapacityInJoules) {
		return new JSSEASBatteryManager(prof, charge, estUptime,batteryCapacityInJoules);
	}

	@Override
	public DefaultExecutionManager createExecutionManager() {
		return new JSSEASExecutionManager();
	}

	@Override
	public DefaultNetworkEnergyManager createNetworkEnergyManager(
			boolean enableNetworkExecutionManager, short wifiSignalStrength) {		
		return new DefaultNetworkEnergyManager(enableNetworkExecutionManager,wifiSignalStrength);
	}

	@Override
	public Device createDevice(String name, BatteryManager bt,	ExecutionManager em, NetworkEnergyManager nem) {		
		return new JSDevice(name,bt,em,nem);
	}

}
