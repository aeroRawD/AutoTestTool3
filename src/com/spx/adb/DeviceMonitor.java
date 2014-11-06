package com.spx.adb;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;
import com.log.Log;

/**
 * 监控当前连接的设备
 * 
 * @author SHAOPENGXIANG
 * 
 */
public class DeviceMonitor extends Thread {
	private Logger logger = Log.getSlientLogger("device");
	private List<IDevice> devices = new ArrayList<IDevice>();
	private List<Device> deviceSerials = new ArrayList<Device>();
	private List<Device> prevDevices = new ArrayList<Device>();
	
	List<Device> added = new ArrayList<Device>();
	List<Device> deleted = new ArrayList<Device>();
	List<Device> updated = new ArrayList<Device>();
	
	
	private boolean started = false;

	class Device {
		String serial;
		String status;
	}

	private static DeviceMonitor sInstance = new DeviceMonitor();

	private DeviceMonitor() {
	};

	public static DeviceMonitor getInstance() {
		return sInstance;
	}

	public void startMonitor() {
		if (started)
			return;
		this.start();
		started = true;
	}

	/**
	 * 返回连接状态的设备数
	 * 
	 * @return
	 */
	public int getOnlineDeviceCount() {
		int count = 0;
		synchronized (deviceSerials) {
			for (Device dev : deviceSerials) {
				if (dev.status.equals("device")) {
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * 返回连接的设备序号
	 * 
	 * @return
	 */
	public List<String> getOnlineDeviceSerials() {
		List<String> serials = new ArrayList<String>();
		synchronized (deviceSerials) {
			for (Device dev : deviceSerials) {
				serials.add(dev.serial);
			}
		}

		return serials;
	}

	public List<IDevice> getOnlineDevices() {
		devices.clear();
		List<String> serials = getOnlineDeviceSerials();
		for (String s : serials) {
			devices.add(DeviceUtil.createDevice(s));
		}
		return devices;
	}

	/**
	 * 判断serial设备是否online
	 * 
	 * @param serial
	 * @return
	 */
	public boolean isDeviceOnline(String serial) {
		synchronized (deviceSerials) {
			for (Device dev : deviceSerials) {
				if (serial.equals(dev.serial)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
    public void run() {
		while (true) {
			
			
			// printDevices();
			try {
				findDevices();
				
				Thread.sleep(12000);
			} catch (Exception e) {
				e.printStackTrace();
				Util.sleep(2000);
			}
		}
	}

	private void updateIDevices() {

	}

	private void printDevices() {
		logger.fine( "当前连接设备数量:" + deviceSerials.size());
		for (Device dev : deviceSerials) {
			Log.d( dev.serial + "\t" + dev.status);
		}
	}

	private void findDevices() {
		List<String> output = Util.getCmdOutput("adb devices");
		// Log.d(TAG, "output.size:"+output.size());
		List<Device> devs = new ArrayList<Device>();
		for (String s : output) {

			if (s.contains("List of devices attached"))
				continue;
			if (s.indexOf("\t") == -1)
				continue;
			s = s.trim();
			// Log.d(TAG, "s:"+s);
			String serial = s.substring(0, s.indexOf("\t")).trim();
			String status = s.substring(s.indexOf("\t")).trim();
			Device dev = new Device();
			dev.serial = serial;
			dev.status = status;
			devs.add(dev);
		}

		synchronized (deviceSerials) {
			prevDevices.clear();
			prevDevices.addAll(deviceSerials);
			deviceSerials.clear();
			deviceSerials.addAll(devs);
		}

		checkDevices();
	}
	
	private void checkDevices(){
		added.clear();
		deleted.clear();
		updated.clear();
		
		for(Device dev:prevDevices){
			boolean found = false;
			boolean same = false;
			for(Device newDev:deviceSerials){
				if(dev.serial.equals(newDev.serial)){
					found = true;
					if(dev.status.equals(newDev.status)){
						same = true;
						break;
					}
				}
			}
			if(!found){
				deleted.add(dev);
			}else if(!same){
				updated.add(dev);
			}
		}
		
		for(Device newDev:deviceSerials){
			boolean found = false;
			for(Device dev:prevDevices){
				if(dev.serial.equals(newDev.serial)){
					found = true;
				}
			}
			if(!found){
				added.add(newDev);
			}
		}
		
		if(added.size()>0){
			String msg = "增加了"+added.size()+"个设备:";
			for(int i=0;i<added.size();i++){
				if(i>0){
					msg+=",";
				}
				msg+=added.get(i).serial;
			}
			logger.info(msg);
		}
		
		
		if(deleted.size()>0){
			String msg = "减少了"+deleted.size()+"个设备:";
			for(int i=0;i<deleted.size();i++){
				if(i>0){
					msg+=",";
				}
				msg+=deleted.get(i).serial;
				DeviceUtil.onDeviceRemove(deleted.get(i).serial);
			}
			logger.info(msg);
		}
	}
}
