package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;

public class TestWifiOnCommand implements TestCommand{

    @Override
    public List<String> getBeforeCommands(String serial) {
        List<String> cmds = new ArrayList<String>();
        cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.openwifi");
        cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.openwifi");
        
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/fails.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/AUTOTEST-ok.xml");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/AUTOTEST-fail.xml");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/ok2.xml");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/fail2.xml");
        return cmds;
    }

    @Override
    public String getCommand(){
        return "am instrument -e wifi on -w com.kingsoft.test/com.kingsoft.test.framework.UseCaseTestRunner";
    }

    @Override
    public List<String> getAfterCommands(String serial) {
        List<String> cmds = new ArrayList<String>();
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/testinfo.xml ./testreport/"+serial+"/testinfo_wifi.xml");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_list.txt ./testreport/"+serial+"/wifion_1_tests_list.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_list.txt");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_result_list.txt ./testreport/"+serial+"/wifion_1_tests_result_list.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_result_list.txt");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_result_errors.txt ./testreport/"+serial+"/wifion_1_tests_result_errors.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_result_errors.txt");
        return cmds;
    }

}
