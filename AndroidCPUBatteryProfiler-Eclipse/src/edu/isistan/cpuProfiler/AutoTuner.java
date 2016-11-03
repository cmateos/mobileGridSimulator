package edu.isistan.cpuProfiler;

import android.util.Log;

public class AutoTuner extends Thread {
	
	public interface TunerListener {
		public void onCPUUsageRead(float cpuUsage,long sleep);
		public void onStable(float cpuUsage);
		public void onUnStable(float cpuUsage);
	}
	
	private float target=0.03f;
	private float threshold=0.02f;
	private boolean alive=true;
	private TunerListener listener;
	private int cpus;

	public AutoTuner() {
		super();
		this.setDaemon(true);
	}
	
	@Override
	public void run() {
		long sleep=1;
		float cpuUsage=0;
		CPUUserThread[] cpuUser=new CPUUserThread[this.cpus];
		for(int i=0;i<this.cpus;i++){
			cpuUser[i]=new CPUUserThread();
			cpuUser[i].setSleep(sleep);
			cpuUser[i].start();
		}
		boolean stable=false;
		boolean nowStable=false;
		float diff=0;
		long sleepNew;
		while(alive){
			cpuUsage=cpuUsage();
			diff=(float)cpuUsage/target;
			Log.i(AndroidCPUBatteryProfilerActivity.LOG_TAG, "CPU Usage: "+cpuUsage+" sleep: "+sleep+" diff: "+diff);
			sleepNew=(long) (sleep*diff);
			nowStable=((-threshold)<(cpuUsage-target))&&((cpuUsage-target)<(threshold));
			if((sleep==sleepNew)
					&&!nowStable){
				if(diff>1) sleep++;
				else sleep--;
			} else sleep=sleepNew;

			synchronized (this) {
				if(listener!=null){
					//Log.i(AndroidCPUBatteryProfilerActivity.LOG_TAG, "Calling Listener "+nowStable);
					listener.onCPUUsageRead(cpuUsage,sleep);
					if(!stable&&nowStable){
						stable=true;
						listener.onStable(cpuUsage);
					}
					if(stable&&!nowStable){
						stable=false;
						listener.onUnStable(cpuUsage);
					}
				}
			}			
			for(int i=0;i<this.cpus;i++)
				cpuUser[i].setSleep(sleep);
		}
		Log.i(AndroidCPUBatteryProfilerActivity.LOG_TAG, "End");
		for(int i=0;i<this.cpus;i++)
			cpuUser[i].kill();
	}
	
	protected synchronized float cpuUsage(){
		float result=0;
		for(int i=0;i<30;i++){
			result+=CPUUtils.readUsage();
			try {
				wait(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result/30;
	}
	
	public void kill(){
		this.alive=false;
	}
	
	public boolean isKilled(){
		return !this.alive;
	}

	public float getTarget() {
		return target;
	}

	public void setTarget(float target) {
		this.target = target;
	}

	public float getThreshold() {
		return threshold;
	}

	public void setThreshold(float threashold) {
		this.threshold = threashold;
	}

	public TunerListener getListener() {
		return listener;
	}

	public synchronized void setListener(TunerListener listener) {
		this.listener = listener;
	}

	public void setCPUs(int cpus) {
		this.cpus=cpus;
	}
	
		
}
