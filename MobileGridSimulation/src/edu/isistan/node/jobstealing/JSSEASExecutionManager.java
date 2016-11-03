package edu.isistan.node.jobstealing;

import edu.isistan.mobileGrid.jobs.Job;
import edu.isistan.mobileGrid.node.SchedulerProxy;
import edu.isistan.node.DefaultExecutionManager;
import edu.isistan.proxy.jobstealing.StealerProxy;

public class JSSEASExecutionManager extends DefaultExecutionManager {

	@Override
	public void onFinishJob(Job job) {
		super.onFinishJob(job);
		if(this.getJobQueueSize()==0 && !isExecuting()){
			((StealerProxy)SchedulerProxy.PROXY).steal(this.getDevice());
		}
	}

}
