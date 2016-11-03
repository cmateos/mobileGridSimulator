package edu.isistan.cpuProfiler;

import edu.isistan.cpuProfiler.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class AndroidCPUBatteryProfilerActivity extends Activity {
	
	public final static String LOG_TAG="CPUBaterryProfiler";
	private ServiceBinder binder;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final TextView tv=(TextView)findViewById(R.id.textView3);
        final EditText target=(EditText)findViewById(R.id.cpuUsage);
        final EditText tr=(EditText)findViewById(R.id.threshold);
        final EditText cpus=(EditText)findViewById(R.id.cpus);
        final CheckBox lock=(CheckBox)findViewById(R.id.lockBox);
        final Handler h=new Handler(){

			@Override
			public void handleMessage(Message msg) {
				tv.setText(AndroidCPUBatteryProfilerActivity.this.getResources().getString(R.string.disconnect));
			}
        	
        };
        final Button stop=(Button)findViewById(R.id.stopButton);
        final Button start=(Button)findViewById(R.id.startButton);
        start.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				start.setEnabled(false);
				Intent i=new Intent(AndroidCPUBatteryProfilerActivity.this, ProfilerService.class);
				AndroidCPUBatteryProfilerActivity.this.bindService(i, 
						new ServiceConnection() {
					
					@Override
					public void onServiceDisconnected(ComponentName name) {
						stop.setEnabled(false);
						start.setEnabled(true);
						target.setEnabled(true);
						tr.setEnabled(true);
						cpus.setEnabled(true);
						lock.setEnabled(true);
						tv.setText("Wait...");
						binder=null;
					}
					
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						stop.setEnabled(true);
						target.setEnabled(false);
						tr.setEnabled(false);
						cpus.setEnabled(false);
						lock.setEnabled(false);
						binder=((ServiceBinder)service);
						binder.setHandler(h, Float.parseFloat(target.getText().toString()), 
								Float.parseFloat(tr.getText().toString()),Integer.parseInt(cpus.getText().toString()),
								lock.isChecked());
						
					}
				}
						, Activity.BIND_AUTO_CREATE);
			}
		});
        stop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				binder.stop();
				tv.setText("Wait...");
				stop.setEnabled(false);
				start.setEnabled(true);
				target.setEnabled(true);
				tr.setEnabled(true);
				cpus.setEnabled(true);
				lock.setEnabled(true);
			}
		});
    }
    @Override
	protected void onStop() {		
		binder.stop();
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		binder.stop();
		super.onDestroy();
	}
}