package edu.isistan.simulator;

import java.io.IOException;
import java.io.OutputStream;

public class Logger {
	
	private static OutputStream DEBUG_OUTPUT_STREAM = null; 
	
	/** The Constant LINE_SEPARATOR. */
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	/**
	 * Set data.separator to choose another way of separating the info in logs
	 */
	private static final String DATA_SEPARATOR = System.getProperty("data.separator")!=null ? System.getProperty("data.separator") : ";";

	/** The output. */
	private static OutputStream OUTPUT;	

	/** The disable output flag. */
	private static boolean ENABLE=true;
	
	public static void enable(){
		ENABLE=true;
	}
	
	public static void disable(){
		ENABLE=false;
	}
	
	public static void setOutput(OutputStream out){
		OUTPUT=out;
	}
	
	private static OutputStream getOutputStream(){
		if(OUTPUT==null)
			return System.out;
		return OUTPUT;
	}
	
	public static void print(String data){
		if(ENABLE)
			try {
				getOutputStream().write(data.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public static void println(String data){
		if(ENABLE)
			try {
				getOutputStream().write(data.getBytes());
				getOutputStream().write(LINE_SEPARATOR.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public static void println(){
		if(ENABLE)
			try {
				getOutputStream().write(LINE_SEPARATOR.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public static void println(Object data){
		if(ENABLE)
			println(data.toString());
	}
	
	public static void print(Object data){
		if(ENABLE)
			print(data.toString());
	}
	
	public static void logEntity(Entity e, String log, Object... data) {
		if(!ENABLE) return;
		StringBuffer logAux=new StringBuffer();
		logAux.append(Simulation.getTime());
		logAux.append(DATA_SEPARATOR);
		logAux.append(e.getName());
		logAux.append(DATA_SEPARATOR);
		logAux.append(log);
		for(Object o:data){
			logAux.append(DATA_SEPARATOR);
			logAux.append(o);			
		}
		println(logAux.toString());
	}
	
	public static void appendDebugInfo(String line){
				
		try {
			DEBUG_OUTPUT_STREAM.write(line.getBytes());
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}

	public static void flushDebugInfo(){
		try {
			DEBUG_OUTPUT_STREAM.flush();
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	public static void setDebugOutputStream(OutputStream debugFile) {
		DEBUG_OUTPUT_STREAM = debugFile;		
	}
}
