package edu.isistan.profiler.tosim;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CPUEvent {
	private long time;
	private double cpuUsage;
	
	public static List<CPUEvent> getEvents(List<String> data){
		List<CPUEvent> cel=new ArrayList<CPUEvent>();
		for(String string:data){
			if(string.contains("CPU")){
				StringTokenizer st=new StringTokenizer(string,",");
				long newTime=Long.parseLong(st.nextToken());
				String aux=st.nextToken();
				aux=aux.substring(10);
				aux=aux.substring(0, aux.indexOf(' '));
				double cpuUsage=Double.parseDouble(aux);
				if(Main.firstMoment<newTime){
					cel.add(new CPUEvent(newTime-Main.firstMoment, cpuUsage));
				}
			}
		}
		cel.remove(cel.size()-1);
		return cel;
	}
	public CPUEvent(long time, double cpuUsage) {
		super();
		this.time = time;
		this.cpuUsage = cpuUsage;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	@Override
	public String toString() {
		DecimalFormat dc=new DecimalFormat("#.########");
		return "NEW_CPU_STATE_NODE;"+time+";CPU_USAGE;"+dc.format(cpuUsage);
	}
	
	
}
