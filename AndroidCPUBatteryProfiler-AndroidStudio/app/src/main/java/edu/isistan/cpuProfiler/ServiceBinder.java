package edu.isistan.cpuProfiler;

import android.os.Binder;
import android.os.Handler;
import android.util.Log;

public class ServiceBinder extends Binder {
	
	private ProfilerService s;
	
	public ServiceBinder(ProfilerService profilerService) {
		s=profilerService;
	}

	public void setHandler(Handler h,float cpuUsage, float threshold, int cpus, boolean lock){
		Log.d(AndroidCPUBatteryProfilerActivity.LOG_TAG, "Starting Service with cpu: "+cpuUsage+" threshold: "+threshold);
		s.setHandler(h,cpuUsage,threshold,cpus,lock);
	}

	public void stop(){
		this.s.stop();
	}
}
