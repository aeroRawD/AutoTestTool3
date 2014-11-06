package com.spx.adb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.NullOutputReceiver;

public class ScreenUi {
	public String LOCAL_PATH = "";
	public String REMOTE_PATH = "";

	private List<UiNode> uiNodeList = new ArrayList<UiNode>();

	private static class UiDumpParser extends MultiLineReceiver {
		public boolean succeed = false;

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public void processNewLines(String[] lines) {
			for (String line : lines) {
				if (line.startsWith("UI hierchary dumped to:")) {
					succeed = true;
				}
			}
		}

	}

	IDevice device = null;
	private static HashMap<IDevice, ScreenUi> screenMap = new HashMap<IDevice, ScreenUi>();

	public static ScreenUi getScreenUiInstance(IDevice device) {
		if (screenMap.get(device) == null) {
			screenMap.put(device, new ScreenUi(device));
		}

		return screenMap.get(device);
	}

	private ScreenUi(IDevice device) {
		this.device = device;
		LOCAL_PATH = "data/" + device.getSerialNumber() + "-uidump.xml";
		REMOTE_PATH = "/sdcard/" + device.getSerialNumber() + "-uidump.xml";
		update();
	}

	public void update() {
		UiDumpParser uiDumpParser = new UiDumpParser();
		Util.runAdbCmd(device, "uiautomator dump " + REMOTE_PATH, uiDumpParser,
				12000);
		if (uiDumpParser.succeed) {
			Util.pullFile(device, REMOTE_PATH, LOCAL_PATH);

			uiNodeList = getData();

		}
	}
	
	public int[] getScreenLocation(String text){
		int[] loc = new int[2];
		UiNode uiNode = getMatchNodeByText(text);
		if (uiNode == null)
			return null;
		int x = uiNode.getX() + uiNode.getWidth() / 2;
		int y = uiNode.getY() + uiNode.getHeight() / 2;
		loc[0] = x;
		loc[1] = y;
		return loc;
	}
	
	public boolean expandStatusBar() {
		// input swipe 100 5 100 200
		Util.runAdbCmd(device, "input swipe 100 5 100 400",
				new NullOutputReceiver(), 4000);
		return true;
	}
	
	public boolean isStatusBarExpanded(){
		return (containText("星期")||containText("周")) && containText("日") && containText("月");
	}
	
	public boolean containText(String text) {
		UiNode uiNode = getMatchNodeByText(text);
		if (uiNode == null)
			return false;
		return true;
	}

	public boolean clickText(String text) {
		UiNode uiNode = getMatchNodeByText(text);
		if (uiNode == null)
			return false;
		int x = uiNode.getX() + uiNode.getWidth() / 2;
		int y = uiNode.getY() + uiNode.getHeight() / 2;

		Util.runAdbCmd(device, "input tap " + x + " " + y,
				new NullOutputReceiver(), 4000);
		return true;
	}

	public List<UiNode> getData() {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File(LOCAL_PATH));
			UiAutomatorDumpXmlParser parser = new UiAutomatorDumpXmlParser();
			List<UiNode> uiNodeList = parser.parse(document);
			return uiNodeList;
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		return null;
	}

	private UiNode getMatchNodeByText(String text) {
		for (UiNode uiNode : uiNodeList) {
			if (text.equals(uiNode.getValue("text"))) {
				return uiNode;
			}
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
