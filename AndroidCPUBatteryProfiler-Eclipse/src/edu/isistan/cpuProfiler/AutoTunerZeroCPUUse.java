package edu.isistan.cpuProfiler;

import android.util.Log;

public class AutoTunerZeroCPUUse extends AutoTuner {

	@Override
	public void run() {
		boolean stable=false;
		boolean nowStable=false;
		while(!this.isKilled()){
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				float cpuUsage=cpuUsage();
				Log.i(AndroidCPUBatteryProfilerActivity.LOG_TAG, "CPU Usage: "+cpuUsage);
				nowStable=cpuUsage<getThreshold();
				if(getListener()!=null){
					//Log.i(AndroidCPUBatteryProfilerActivity.LOG_TAG, "Calling Listener "+nowStable);
					getListener().onCPUUsageRead(cpuUsage,0);
					if(!stable&&nowStable){
						stable=true;
						getListener().onStable(cpuUsage);
					}
					if(stable&&!nowStable){
						stable=false;
						getListener().onUnStable(cpuUsage);
					}
				}
			}
		}
	}
}
