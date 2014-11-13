package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;

public class TestWifiOffRetryCommand extends TestCommandAdapter{

    public List<String> getBeforeCommands(String serial){
        List<String> cmds = new ArrayList<String>();
        cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.closewifi");
        cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.closenetwork");
        return cmds;
    }

    
    public String getCommand(){
        return "am instrument -e ouput_ok ok2.xml -e ouput_fail fail2.xml -w com.kingsoft.test/com.kingsoft.test.framework.FailedCaseRetryTestRunner";
    }


    @Override
    public List<String> getAfterCommands(String serial) {
        /**
         * adb pull /sdcard/powerword/AUTOTEST-ok.xml ./testreport/testresult_ok_nowifi.xml
adb pull /sdcard/powerword/ok2.xml ./testreport/testresult_ok_nowifi2.xml
adb pull /sdcard/powerword/fail2.xml ./testreport/testresult_fail_nowifi2.xml
         */
        List<String> cmds = new ArrayList<String>();
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/AUTOTEST-ok.xml  testreport/"+serial+"/testresult_ok_nowifi.xml");
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/ok2.xml  testreport/"+serial+"/testresult_ok_nowifi2.xml");
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/fail2.xml  testreport/"+serial+"/testresult_fail_nowifi2.xml");
        
        //cmds.add("adb -s "+ serial+" shell cat /proc/meminfo > testreport/"+serial+"/meminfo.txt");
        //cmds.add("adb -s "+ serial+" shell getprop > testreport/"+serial+"/prop.txt");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_list.txt ./testreport/"+serial+"/wifioff_2_tests_list.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_list.txt");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_result_list.txt ./testreport/"+serial+"/wifioff_2_tests_result_list.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_result_list.txt");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_result_errors.txt ./testreport/"+serial+"/wifioff_2_tests_result_errors.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_result_errors.txt");
        return cmds;
    }
 }
