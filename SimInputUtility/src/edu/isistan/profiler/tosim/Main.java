package edu.isistan.profiler.tosim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static long firstMoment;
	
	public static void main(String[] args){
		List<String> data=fileToStringList(new File(args[0]));
		List<String> bel=toStringList(BatteryEvent.getEvents(data));
		List<String> cel=toStringList(CPUEvent.getEvents(data));
		bel.add(0, "ADD_NODE;1");
		bel.add("LEFT_NODE;"+getLastEventTime(data));
		stringListToFile(bel, new File(args[1]));
		stringListToFile(cel, new File(args[2]));
	}
	
	public static List<String> fileToStringList(File f){
		try {
			BufferedReader br=new BufferedReader(new FileReader(f));
			List<String> result=new ArrayList<String>();
			String line=br.readLine();
			while (line!=null) {
				result.add(line);
				line=br.readLine();
			}
			br.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public static void stringListToFile(List<String> st,File f){
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(f));
			for (String string : st) {
				bw.write(string+"\n");
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static List<String> toStringList(List<?> data){
		List<String> results=new ArrayList<String>(data.size());
		for(Object t:data){
			results.add(t.toString());
		}
		return results;
	}
	
	public static long getLastEventTime(List<String> data){
		String let=data.get(data.size()-1);
		let=let.substring(0, let.indexOf(','));
		return Long.parseLong(let)-Main.firstMoment;
	}
}
