package edu.isistan.reader;

import edu.isistan.mobileGrid.node.BatteryManager;
import edu.isistan.mobileGrid.node.Device;
import edu.isistan.mobileGrid.node.ExecutionManager;
import edu.isistan.mobileGrid.node.NetworkEnergyManager;
import edu.isistan.node.DefaultBatteryManager;
import edu.isistan.node.DefaultExecutionManager;
import edu.isistan.node.DefaultNetworkEnergyManager;

public interface ManagerFactory {

	public DefaultBatteryManager createBatteryManager(int prof, int charge, long estUptime, long batteryCapacityInJoules);
	
	public DefaultExecutionManager createExecutionManager();

	public DefaultNetworkEnergyManager createNetworkEnergyManager(boolean enableNetworkExecutionManager, short wifiSignalString);
	
	public Device createDevice(String name, BatteryManager bt, ExecutionManager em,	NetworkEnergyManager nem);

}
