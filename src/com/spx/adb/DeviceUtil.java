package com.spx.adb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;

public class DeviceUtil {
	private static HashMap<String, IDevice> deviceMap = new HashMap<String, IDevice>();

	public static IDevice getDevice(String serial) {
		if (deviceMap.get(serial) == null) {
			IDevice dev = createDevice(serial);
			if (dev == null)
				return null;
			deviceMap.put(serial, dev);
		}
		
		IDevice d = deviceMap.get(serial);
		if(!d.isOnline()){
			IDevice dev = createDevice(serial);
			if (dev == null)
				return null;
			deviceMap.put(serial, dev);
		}
		
		
		return deviceMap.get(serial);
	}
	
	public static IDevice getDefaultDevice() {
		IDevice[] devices = getDevices();
		if (devices == null || devices.length == 0)
			return null;
		return devices[0];
	}
	
	public static List<IDevice> getOnlineDevices(){
		List<IDevice> devs = new ArrayList<IDevice>();
		Set<String> keySet = deviceMap.keySet();
		Iterator<String> iterator = keySet.iterator();
		while(iterator.hasNext()){
			String serial = iterator.next();
			devs.add(deviceMap.get(serial));
		}
		
		return devs;
	}
	
	public static IDevice[] getDevices(){
		AndroidDebugBridge bridge = AndroidDebugBridge
				.createBridge("adb", true);// 如果代码有问题请查看API，修改此处的参数值试一下
		waitDevicesList(bridge);
		IDevice devices[] = bridge.getDevices();
		if (devices == null)
			return null;
		
		return devices;
	}

	/**
	 * 获取得到device对象
	 * 
	 * @return
	 */
	public static IDevice createDevice(String serial) {
		IDevice devices[] = getDevices();
		if (devices == null)
			return null;

		for (IDevice dev : devices) {
			if (dev != null && serial.equalsIgnoreCase(dev.getSerialNumber())) {
				return dev;
			}
			
			if(dev != null && serial.equals("")){
				return dev;
			}
		}
		return null;
	}
	
	

	/**
	 * 等待查找device
	 * 
	 * @param bridge
	 */
	private static void waitDevicesList(AndroidDebugBridge bridge) {
		int count = 0;
		while (bridge.hasInitialDeviceList() == false) {
			try {
				Thread.sleep(500);
				count++;
			} catch (InterruptedException e) {
			}
			if (count > 240) {
				System.err.print("等待获取设备超时");
				break;
			}
		}
	}
	
	public static void main(String[] args){
		AndroidDebugBridge.init(false);
		IDevice devices[] = getDevices();
		for(IDevice dev:devices){
			System.err.print("dev:"+dev.getName()+", serial:"+dev.getSerialNumber());
		}
		AndroidDebugBridge.terminate();
	}
}
