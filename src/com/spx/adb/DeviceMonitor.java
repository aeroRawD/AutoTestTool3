package com.spx.adb;

import java.util.ArrayList;
import java.util.List;

import com.android.ddmlib.IDevice;

/**
 * 监控当前连接的设备
 * 
 * @author SHAOPENGXIANG
 * 
 */
public class DeviceMonitor extends Thread {
	private static final String TAG = "DeviceMonitor";
	private List<IDevice> devices = new ArrayList<IDevice>();
	private List<Device> deviceSerials = new ArrayList<Device>();
	private List<Device> prevDevices = new ArrayList<Device>();
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

	public void run() {
		while (true) {
			findDevices();
			// printDevices();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateIDevices() {

	}

	private void printDevices() {
		Log.d(TAG, "当前连接设备数量:" + deviceSerials.size());
		for (Device dev : deviceSerials) {
			Log.d(TAG, dev.serial + "\t" + dev.status);
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

	}
}
