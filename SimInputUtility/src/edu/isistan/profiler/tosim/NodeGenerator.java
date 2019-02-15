package edu.isistan.profiler.tosim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

public class NodeGenerator {

	private static long ID=1;
	private static List<String> battery;
	private static List<String> fullBattery;
	private static List<String> cpu;

	private static List<String> node;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		battery=new ArrayList<String>();
		fullBattery=new ArrayList<String>();
		cpu=new ArrayList<String>();
		node=new ArrayList<String>();
		battery.add(";batteryFile");
		fullBattery.add(";batteryFullCpuUsageFile");
		cpu.add(";cpuFile");
		List<String> data=Main.fileToStringList(new File(args[0]));
		for(String st:data)
			addConfig(st);
		for(String st:battery)
			System.out.println(st);
		System.out.println();
		for(String st:fullBattery)
			System.out.println(st);
		System.out.println();
		for(String st:cpu)
			System.out.println(st);
		System.out.println();
		for(String st:node)
			System.out.println(st);
		
	}

	private static void addConfig(String data) {
		StringTokenizer st=new StringTokenizer(data, ",");
		int cant=Integer.parseInt(st.nextToken());
		int flops=Integer.parseInt(st.nextToken());
		String bat=st.nextToken();
		String fbat=st.nextToken();
		String cpu=st.nextToken();
		for(int i=0;i<cant;i++){
			UUID uuid=new UUID(0, ID);
			ID++;
			battery.add(bat+"; "+uuid);
			fullBattery.add(fbat+"; "+uuid);
			NodeGenerator.cpu.add(cpu+"; "+uuid);
			node.add(uuid.toString()+";"+flops);
		}
	}

}
