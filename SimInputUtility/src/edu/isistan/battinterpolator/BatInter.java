package edu.isistan.battinterpolator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class BatInter {
	private static int MAX=100;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> list=readFile(new File(args[0]));
		List<String> result=new ArrayList<String>();
		String in=null;
		long tIn=0;
		long taeIn=0;
		long bIn=0;
		String fin=null;
		long tFin=0;
		long taeFin=0;
		long bFin=0;
		for(String l:list){
			if(l.startsWith("NEW_BATTERY_STATE_NODE")){
				if(in==null){
					in=l;
					StringTokenizer t=new StringTokenizer(l,";");
					for(int i=0;i<4;i++){
						String tok=t.nextToken();
						if(i==1)
							tIn=Long.parseLong(tok);
						if(i==2)
							taeIn=Long.parseLong(tok);
						if(i==3)
							bIn=Long.parseLong(tok);
					}
				}  else {
					fin=l;
					StringTokenizer t=new StringTokenizer(l,";");
					for(int i=0;i<4;i++){
						String tok=t.nextToken();
						if(i==1)
							tFin=Long.parseLong(tok);
						if(i==2)
							taeFin=Long.parseLong(tok);
						if(i==3)
							bFin=Long.parseLong(tok);
					} 
					for(int i=1;i<MAX;i++){
						int maxmi=MAX-i;
						long ti=(tIn*maxmi+tFin*i)/MAX;
						long tae=(taeIn*maxmi+taeFin*i)/MAX;
						long b=(bIn*maxmi+bFin*i)/MAX;
						result.add("NEW_BATTERY_STATE_NODE;"+ti+";"+tae+";"+b);
					}
					in=fin;
					tIn=tFin;
					taeIn=taeFin;
					bIn=bFin;
				}
			}
			result.add(l);
		}
		for(String l:result)
			System.out.println(l);
	}

	public static List<String> readFile(File f) {
		List<String> result=new ArrayList<String>();
		try {
			BufferedReader bf=new BufferedReader(new FileReader(f));
			String line=bf.readLine();
			while(line!=null){
				result.add(line);
				line=bf.readLine();
			}
			bf.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
