package com.att.build;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;
import com.att.testcommand.TestCommand;
import com.att.testcommand.TestWifiOffCommand;
import com.att.testcommand.TestWifiOnCommand;
import com.log.Log;
import com.spx.adb.DeviceUtil;
import com.spx.adb.Util;

public class NotUsed {
    private Logger logger = Log.getSlientLogger("DailyRunner");
    private void startSetupTestEnv_fail(){
        logger.info("startSetupTestEnv ....");
//        runCmd("adb install -r Yzc.apk");
//        runCmd("adb shell am start -n com.example.yzc/com.example.yzc.MainActivity");
//        runCmd("adb shell am broadcast -a kingsoft.test.cmd.action.closewifi");
//        runCmd("adb shell am broadcast -a kingsoft.test.cmd.action.closenetwork");
//        
//        runCmd("adb uninstall com.kingsoft.test");
//        runCmd("adb uninstall com.kingsoft");
//        
//        runCmd("adb shell rm /sdcard/powerword/AUTOTEST-all.xml");
//        runCmd("adb shell rm /sdcard/powerword/testinfo.xml");
//        runCmd("adb shell rm -r /sdcard/Robotium-Screenshots");
//        runCmd("adb shell rm /sdcard/powerword/testinfo.xml");
        List<IDevice> onlineDevices = DeviceUtil.getOnlineDevices();
        
        List<IDevice> setupSucceedDevices = new ArrayList<IDevice>();
        for (IDevice device : onlineDevices) {
            logger.info("startSetupTestEnv ....name:"+device.getName()+",serial:"+device.getSerialNumber());
            if(!setupTestEnv(device)){
                logger.severe("为设备"+device.getName()+"设置测试环境失败.");
            } else{
                setupSucceedDevices.add(device);
            }
        }
        
        //执行测试命令
        for (IDevice device : setupSucceedDevices) {
            runTestCaseForDevice(device);
        }
        Util.printAllThreadStack();
    }
    
    private boolean runTestCaseForDevice(IDevice device){
        logger.info("startSetupTestEnv ...");
        if(!device.isOnline()){
            device = DeviceUtil.createDevice(device.getSerialNumber());
        }
        List<TestCommand> testCommands = new ArrayList<TestCommand>();
        testCommands.add(new TestWifiOffCommand());
        testCommands.add(new TestWifiOnCommand());
        //testCommand.addCommand("am instrument -e wifi off -w com.kingsoft.test/com.kingsoft.test.framework.UseCaseTestRunner");
        //testCommand.addCommand("am instrument -w -e class com.kingsoft.test.api.ApiTest#testApi com.kingsoft.test/android.test.InstrumentationTestRunner");
        
        //每个设备都要新启动一个线程运行测试用例.
//        TestCaseRunner runner = new TestCaseRunner(device.getSerialNumber(), testCommands, (TestcaseRunningListener) this);
//        runner.start();
        
        return true;
    }
    
    private boolean setupTestEnv(IDevice device){
        boolean succeed = Util.installApk(device, "Yzc.apk", "com.example.yzc");
        if(!succeed){
            logger.severe("安装 Yzc.apk失败.");
            return false;
        }
        
        Util.runAdbCmdGetReturn(device, "am start -n com.example.yzc/com.example.yzc.MainActivity", 4000);
        closeNetwork(device);
        
        succeed = Util.removePackage(device, "com.kingsoft.test");
        if(!succeed){
            logger.severe("卸载com.kingsoft.test失败.");
            //return false;
        }
        succeed = Util.removePackage(device, "com.kingsoft");
        if(!succeed){
            logger.severe("卸载com.kingsoft失败.");
            //return false;
        }
        
        cleanUpTestouput(device);
        
        //安装
        List<String> installedDevices = Util.installPowerwordAppToAllOnlineDevices();
        if(installedDevices.size()==0){
            logger.severe("安装Powerword应用失败.");
            return false;
        }
        
        installedDevices = Util.installTestAppToAllOnlineDevices();
        if(installedDevices.size()==0){
            logger.severe("安装测试应用失败.");
            return false;
        }
        
        return true;
    }
    
    private void closeNetwork(IDevice device){
        Util.runAdbCmdGetReturn(device, "am broadcast -a kingsoft.test.cmd.action.closewifi", 4000);
        Util.runAdbCmdGetReturn(device, "am broadcast -a kingsoft.test.cmd.action.closenetwork", 4000);
    }
    private void cleanUpTestouput(IDevice device){
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/AUTOTEST-all.xml", 4000);
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/testinfo.xml", 4000);
        Util.runAdbCmdGetReturn(device, "rm -r /sdcard/Robotium-Screenshots", 4000);
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/testinfo.xml", 4000);
        
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/fails.txt", 4000);
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/AUTOTEST-ok.xml", 4000);
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/AUTOTEST-fail.xml", 4000);
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/ok2.xml", 4000);
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/fail2.xml", 4000);
        Util.runAdbCmdGetReturn(device, "rm /sdcard/powerword/ok2.xml", 4000);
    }
//    private void checkOperation(boolean ret){
//        if(!ret)
//    }
}
