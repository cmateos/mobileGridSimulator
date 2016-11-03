package edu.isistan;

import java.lang.Thread.UncaughtExceptionHandler;

import edu.isistan.mobileGrid.jobs.JobStatsUtils;
import edu.isistan.mobileGrid.network.IdealBroadCastLink;
import edu.isistan.mobileGrid.network.NetworkModel;
import edu.isistan.mobileGrid.network.SimpleNetworkModel;
import edu.isistan.mobileGrid.persistence.IPersisterFactory;
import edu.isistan.mobileGrid.persistence.DBEntity.DeviceTuple;
import edu.isistan.mobileGrid.persistence.DBEntity.JobStatsTuple;
import edu.isistan.persistence.mybatis.MybatisPersisterFactory;
import edu.isistan.reader.DeviceReader;
import edu.isistan.reader.SimReader;

public class Simulation {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length==3){
			Object o=new Object();
			System.err.println("Waiting for 10 sec");
			synchronized (o) {
				try {
					o.wait(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			o=null;
			System.err.println("Executing");
		}		
		setPersisters();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread arg0, Throwable arg1) {
				arg1.printStackTrace();
				System.exit(1);
			}
		});
		
		//uncomment for debugging
		/**OutputStream debugFile = null;
		try {
			debugFile = new FileOutputStream("DebugLog.log");
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}		
		Logger.setDebugOutputStream(debugFile);
		*/
		
		((SimpleNetworkModel)NetworkModel.getModel()).setDefaultLink(new IdealBroadCastLink());
		
		boolean storeInDB=false;
		if (args.length==2) storeInDB = Boolean.parseBoolean(args[1]);
		
		SimReader sr=new SimReader();
		sr.read(args[0], storeInDB);
		JobStatsUtils.setSim_id(SimReader.getSim_id());
		edu.isistan.simulator.Simulation.runSimulation();
		
		
		if (storeInDB)
			JobStatsUtils.storeInDB();		
		
		//Logger.flushDebugInfo();
		
		JobStatsUtils.printNodeInformationSummaryByNodeMips();
		System.out.print("Total simulated time: ");
		System.out.println(edu.isistan.simulator.Simulation.getTime());
		System.out.println(JobStatsUtils.timeToHours(edu.isistan.simulator.Simulation.getTime()));
		System.out.print("Jobs simulated: ");
		System.out.println(JobStatsUtils.getSize());
		System.out.print("Jobs successfully executed: ");
		System.out.println(JobStatsUtils.getSuccessfullyExecutedJobs());		
		System.out.print("Successfully execution time: ");
		System.out.println(JobStatsUtils.getEffectiveExecutionTime());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getEffectiveExecutionTime()));
		System.out.print("Successfully execution time per effective job: ");
		int execJobs = JobStatsUtils.getSuccessfullyExecutedJobs();
		if (execJobs > 0){
			System.out.println(JobStatsUtils.getEffectiveExecutionTime()/execJobs);
			System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getEffectiveExecutionTime()/execJobs));
		}
		else{
			System.out.println(JobStatsUtils.timeToHours(execJobs));
			System.out.println(JobStatsUtils.timeToHours(execJobs));
		}
		
		
		System.out.print("Executed job waiting time: ");
		System.out.println(JobStatsUtils.getEffectiveQueueTime());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getEffectiveQueueTime()));
		System.out.print("Executed job waiting time per effective: ");
		if (execJobs > 0){
			System.out.println(JobStatsUtils.getEffectiveQueueTime()/JobStatsUtils.getSuccessfullyExecutedJobs());
			System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getEffectiveQueueTime()/JobStatsUtils.getSuccessfullyExecutedJobs()));
		}
		else{
			System.out.println(execJobs);
			System.out.println(JobStatsUtils.timeToHours(execJobs));
		}

		System.out.println("*****************************");
		System.out.print("Total queue time: ");
		System.out.println(JobStatsUtils.getTotalQueueTime());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalQueueTime()));
		System.out.print("Average queue time per job: ");
		System.out.println(JobStatsUtils.getTotalQueueTime()/JobStatsUtils.getSize());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalQueueTime()/JobStatsUtils.getSize()));
		System.out.print("Total execution time: ");
		System.out.println(JobStatsUtils.getTotalExecutionTime());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalExecutionTime()));
		System.out.print("Average execution time per job: ");
		System.out.println(JobStatsUtils.getTotalExecutionTime()/JobStatsUtils.getSize());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalExecutionTime()/JobStatsUtils.getSize()));
		
		System.out.println("*****************************");
		int failed = JobStatsUtils.getSize() - JobStatsUtils.getSuccessfullyExecutedJobs();
		long wastedWaited = JobStatsUtils.getTotalQueueTime() - JobStatsUtils.getEffectiveQueueTime();
		long wastedExecution = JobStatsUtils.getTotalExecutionTime() - JobStatsUtils.getEffectiveExecutionTime();
		System.out.print("Failed jobs: ");
		System.out.println(failed);
		System.out.print("Wasted queue time: ");
		System.out.println(wastedWaited);
		System.out.println(JobStatsUtils.timeToHours(wastedWaited));
		System.out.print("Average wasted queue time per failed job: ");		 
		long wastedDivFailed = failed != 0 ? wastedWaited/failed : 0; 
		System.out.println(wastedDivFailed);
		String wasteDivFailedInHours = failed != 0 ? JobStatsUtils.timeToHours(wastedWaited/failed): "0";
		System.out.println(wasteDivFailedInHours);
		System.out.print("Wasted execution time: ");
		System.out.println(wastedExecution);
		System.out.println(JobStatsUtils.timeToHours(wastedExecution));
		System.out.print("Average wasted execution time per failed job: ");
		wastedDivFailed = failed != 0 ? wastedExecution/failed : 0;
		System.out.println(wastedDivFailed);
		wasteDivFailedInHours = failed != 0 ? JobStatsUtils.timeToHours(wastedExecution/failed): "0"; 
		System.out.println(wasteDivFailedInHours);		
		
		System.out.println("*****************************");
		System.out.print("Total transfers: ");
		System.out.println(JobStatsUtils.cantJobTrasnfers());
		System.out.print("Total stealings: ");		
		System.out.println(JobStatsUtils.getTotalStealings());
		System.out.print("Total transfer time: ");
		System.out.println(JobStatsUtils.getTotalTransferTime());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalTransferTime()));
		System.out.print("Total transfer time per job: ");
		System.out.println(JobStatsUtils.getTotalTransferTime()/JobStatsUtils.getSize());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalTransferTime()/JobStatsUtils.getSize()));
		System.out.println("*****************************");
		System.out.print("Total results transfers: ");
		System.out.println(JobStatsUtils.cantJobResultTransfers());
		System.out.print("Total result transfer time: ");
		System.out.println(JobStatsUtils.getTotalResultsTransferTime());
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalResultsTransferTime()));
		System.out.print("Total result transfer time per job: ");
		if (execJobs > 0){
			System.out.println(JobStatsUtils.getTotalResultsTransferTime()/JobStatsUtils.getSuccessfullyExecutedJobs());
			System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.getTotalResultsTransferTime()/JobStatsUtils.getSuccessfullyExecutedJobs()));
		}
		else{
			System.out.println(execJobs);
			System.out.println(JobStatsUtils.timeToHours(execJobs));
		}
			
		System.out.println("*****************************");
		System.out.println("Net stats summary");
		System.out.println("-------------------");
		System.out.println("Total Percentage of energy consumed in data transmisions: "+JobStatsUtils.getPercentageOfEnergyInDataTransmision());
		System.out.println("Percentage sending:"+JobStatsUtils.getPercentageOfEnergyInSendingData());
		System.out.println("Percentage receiving:"+JobStatsUtils.getPercentageOfEnergyInReceivingData());		
		System.out.print("Total update messages received by the proxy:");		
		System.out.println(JobStatsUtils.getTotalUpdateMsgReceivedByProxy());
		System.out.print("Total update messages sent by nodes:");
		System.out.println(JobStatsUtils.getTotalUpdateMsgSentByNodes());
		System.out.print("Amount of sent data (in Gb):");
		System.out.println(JobStatsUtils.getTotalTransferedData(true)/1024);
		System.out.print("Amount of received data (in Gb):");
		System.out.println(JobStatsUtils.getTotalTransferedData(false)/1024);
		System.out.print("Total job data input (in Gb):");
		System.out.println(JobStatsUtils.getAggregatedJobsData(true));
		System.out.print("Total job data output (in Gb):");
		System.out.println(JobStatsUtils.getAggregatedJobsData(false));				
		System.out.println("*****************************");		
		System.out.println(JobStatsUtils.printNodesPercentageOfEnergyWasteInNetworkActivity());
		System.out.println("Jobs states summary");
		System.out.println("-------------------");
		JobStatsUtils.printJobStatesSummary();
		System.out.print("Percentage of completed jobs:");
		System.out.println(((((Integer)(JobStatsUtils.getCompletedJobs()*100)).floatValue()))/((Integer)JobStatsUtils.getSize()).floatValue());
		System.out.print("Nodes iddle Time:");		
		System.out.println(JobStatsUtils.timeToHours(JobStatsUtils.devicesIddleTime));
		System.out.print("Total executed ops (in GIPs):");		
		System.out.println(JobStatsUtils.getTotalExecutedGIP());
		//System.out.println("*****************************");
		//System.out.println(((SimpleGASchedulerProxy)SchedulerProxy.PROXY).printGeneticRoundsInfo());		
		
	}

	private static void setPersisters() {
		IPersisterFactory pf = new MybatisPersisterFactory();
		DeviceReader.setPersisterFactory(pf);
		JobStatsTuple.setIPersisterFactory(pf);
		JobStatsUtils.persisterFactory = pf;
		DeviceTuple.setIPersisterFactory(pf);
		SimReader.setPersisterFactory(pf);
		
	}

}
