package edu.isistan.cpuProfiler;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.widget.RemoteViews;

public class ProfilerService extends Service {
	
	private BatteryReceiver breceiver;
	private AutoTuner at;
	private Logger logger;
	private PowerManager.WakeLock lockReal;
	
	private Handler viewHandler;
	private Handler serviceHandler;
	private float cpuUsage;
	private float threshold;
	private int cpus;
	private boolean lock;

	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceBinder(this);
	}

	
	
	public void setHandler(final Handler h,float cpuUsage, float threshold, int cpus, boolean lock) {
		this.viewHandler=h;
		this.cpuUsage=cpuUsage;
		this.threshold=threshold;
		this.cpus=cpus;
		this.lock=lock;
		this.serviceHandler.sendEmptyMessage(0);
	}



	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate() {
		super.onCreate();
		PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);
		this.lockReal=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AndroidCPUBatteryProfilerActivity.LOG_TAG);
		this.serviceHandler=new Handler(){

			@SuppressWarnings("deprecation")
			@Override
			public void handleMessage(Message msg) {
				if(lock)lockReal.acquire();
				Notification notification=new Notification();
				notification.icon=R.drawable.battery;
				notification.flags=Notification.FLAG_ONGOING_EVENT;
				notification.tickerText="Profiling...";
				notification.contentView=new RemoteViews(getPackageName(),R.layout.notification);
				notification.when=System.currentTimeMillis();
				notification.contentIntent=PendingIntent.getActivity(ProfilerService.this,0, new Intent("NO-OP"), 0);
				startForeground(1, notification);
				
				logger=new Logger(Environment.getExternalStorageDirectory().getAbsolutePath()+"/CpuBatProfile.txt");
				
				ProfilerService.this.breceiver=new BatteryReceiver(logger);
				IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				ProfilerService.this.registerReceiver(ProfilerService.this.breceiver, filter);
				
				ProfilerService.this.at=new AutoTuner();
				if(cpuUsage==0f)ProfilerService.this.at=new AutoTunerZeroCPUUse();
				ProfilerService.this.at.setThreshold(threshold);
				ProfilerService.this.at.setTarget(cpuUsage);
				ProfilerService.this.at.setCPUs(cpus);
				ProfilerService.this.at.setListener(new AutoTuner.TunerListener() {
					
					@Override
					public void onUnStable(float cpuUsage) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onStable(float cpuUsage) {
						ProfilerService.this.viewHandler.sendEmptyMessage(1);
					}
					
					@Override
					public void onCPUUsageRead(float cpuUsage, long sleep) {
						logger.write("CPUUsage: "+cpuUsage+ " Sleep: "+sleep);
					}
				});
				at.start();
			}
			
		};
	}
	
	public void stop(){
		if(this.at==null) return;
		this.at.setListener(null);
		this.at.kill();
		this.at=null;
		this.unregisterReceiver(this.breceiver);
		this.breceiver=null;
		this.logger.flush();
		this.logger=null;
		this.stopForeground(true);
		if(lockReal.isHeld())lockReal.release();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.stop();
	}



	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if(this.logger!=null) this.logger.flush();
	}
	
	
	
}
