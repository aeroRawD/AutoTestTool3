package com.att.report;

import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class UseCase {
    Element mElement = null;
    private HashMap<String, String> attributes = new HashMap<String, String>();

    private boolean pass = true;

    public UseCase(Element e) {
        mElement = e;
        init();
    }

    private void init() {
        for (Iterator i = mElement.attributeIterator(); i.hasNext();) {
            Attribute attribute = (Attribute) i.next();
            // System.out.println("att.name:"+attribute.getName()+", att.text:"+attribute.getText()+",att.value:"+attribute.getValue());
            attributes.put(attribute.getName(), attribute.getValue());
        }
        if (mElement.element("failure") != null) {
            pass = false;
        }
    }
    
    public boolean isWifiRequired(){
        String wifiAttr =getAttr("wifi");
        if("on".equals(wifiAttr) || "both".equals(wifiAttr)){
            return true;
        }
        return false;
    }

    public boolean isPassed() {
        return pass;
    }

    public String getAttr(String attrName) {
        return attributes.get(attrName);
    }
    
    private String error = "";
    private String stack = "";
    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getError() {
        return error;
    }

    public void setError(String error){
        this.error = error;
    }
    
    public void setPassed(boolean ps){
        pass = ps;
    }
}
