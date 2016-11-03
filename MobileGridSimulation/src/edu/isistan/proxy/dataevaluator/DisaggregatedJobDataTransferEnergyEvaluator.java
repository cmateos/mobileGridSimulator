package edu.isistan.proxy.dataevaluator;

import edu.isistan.node.DefaultBatteryManager;

public class DisaggregatedJobDataTransferEnergyEvaluator implements
		DataAssignmentEvaluatorIF {

	private int maxAvailable = 100;
	
	/**maxAvailable is the maximum battery percentage that could be used for transferring data. The value should be indicated with a positive integer
	 * within the range of [0-100]*/
	public DisaggregatedJobDataTransferEnergyEvaluator(int maxAvailable) {		
			this.maxAvailable = maxAvailable >= 0 && maxAvailable <= 100 ? maxAvailable : 100;			
	}

	@Override
	public double eval(DataAssignment da) {
		//double costPerKb = getCostInJoulesPerKbOfData(da.getDevice());
		int completelyTransferedJobs=0;
		double totalDataTransfered=0.0d;
		double energyRequired=0;
		double energyWasted = 0;
		double nodeAvailableEnergy = ((double)((da.getDevice().getLastBatteryLevelUpdate() / DefaultBatteryManager.PROFILE_ONE_PERCENT_REPRESENTATION) * da.getDevice().getTotalBatteryCapacityInJoules())) / (double)(100);
		double nodeMaxAvailableForTransferring = ((double)maxAvailable * nodeAvailableEnergy) / (double)100;
		for (int job_index = 0; job_index < da.getAssignedJobs().size(); job_index++) {
			energyRequired+=  da.getDevice().getEnergyWasteInTransferingData(da.getAssignedJobs().get(job_index).getInputSize());
			energyRequired+= da.getDevice().getEnergyWasteInTransferingData(da.getAssignedJobs().get(job_index).getOutputSize());
			if (energyRequired <= nodeMaxAvailableForTransferring){
				completelyTransferedJobs++;
				//data transfer is indicated in kilobytes
				totalDataTransfered+=(da.getAssignedJobs().get(job_index).getInputSize() + da.getAssignedJobs().get(job_index).getOutputSize()) / (double)1024;
				energyWasted=energyRequired;
			}
		}		
		da.setDeviceEnergyWasted(energyWasted);
		da.setAffordableDataTranfered(totalDataTransfered);
		da.setAffordableJobCompletelyTransfered(completelyTransferedJobs);
		return energyRequired;		
	}
	
	/**return the mean amount of kilobytes of data that the device is able to transfer with its current 
	 * energy and wifi signal*/
	/*public double getDeviceDataCapacity(Device d){
		//TODO: return a mean value
		double nodeAvailableEnergy = ((double)(d.getLastBatteryLevelUpdate() / DefaultBatteryManager.PROFILE_ONE_PERCENT_REPRESENTATION) * d.getTotalBatteryCapacityInJoules()) / (double)100;
		return nodeAvailableEnergy / getCostInJoulesPerKbOfData(d);
	}*/
	
}
