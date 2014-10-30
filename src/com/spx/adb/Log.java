package com.spx.adb;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public static void d(String str){
		System.out.println((sdf.format(new Date()))+"\t"+str);
	}
	
	public static void d(String tag, String str){
		System.out.println((sdf.format(new Date()))+"\t"+tag+"\t"+str);
	}

}
