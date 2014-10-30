package com.spx.adb;

public class SvnUtil {
	public static String getValue(String line, String prefix){
		if(Util.isNull(line)) return null;
		if(!line.startsWith(prefix)) return null;
		if(line.length()<=prefix.length()+1) return null;
		return line.substring(prefix.length()+1).trim();
	}
}
