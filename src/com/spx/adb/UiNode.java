package com.spx.adb;

import java.util.StringTokenizer;

import org.dom4j.Element;

public class UiNode {
	Element element = null;
	String bounds = "";
	int x, y, x2, y2;
	
	public UiNode(Element ele) {
		element = ele;
		bounds = this.getValue("bounds");
		if(bounds.contains("[") && bounds.contains("]") && bounds.contains(",")&& bounds.length()>10){
			java.util.StringTokenizer st = new StringTokenizer(bounds, "[],");
			int count = st.countTokens();
			if(count ==4){
				x= Integer.parseInt(st.nextToken());
				y= Integer.parseInt(st.nextToken());
				x2= Integer.parseInt(st.nextToken());
				y2= Integer.parseInt(st.nextToken());
			}
		}
		
	}

	public String getValue(String attr) {
		return this.element.attributeValue(attr);
	}
	
	
	public int getX(){
		return x;
	}

	public int getY(){
		return y;
	}
	
	public int getWidth(){
		return x2-x;
	}
	
	public int getHeight(){
		return y2-y;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
