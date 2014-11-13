package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;


public class TestWifiOffCommand extends TestCommandAdapter{

   public List<String> getBeforeCommands(String serial){
       List<String> cmds = new ArrayList<String>();
       cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.closewifi");
       cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.closenetwork");
       return cmds;
   }

   
   public String getCommand(){
       return "am instrument -e wifi off -w com.kingsoft.test/com.kingsoft.test.framework.UseCaseTestRunner";
   }

    @Override
    public List<String> getAfterCommands(String serial) {
        List<String> cmds = new ArrayList<String>();
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/testinfo.xml ./testreport/"+serial+"/testinfo_nowifi.xml");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_list.txt ./testreport/"+serial+"/wifioff_1_tests_list.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_list.txt");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_result_list.txt ./testreport/"+serial+"/wifioff_1_tests_result_list.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_result_list.txt");
        
        cmds.add("adb -s "+ serial+" pull /sdcard/powerword/tests_result_errors.txt ./testreport/"+serial+"/wifioff_1_tests_result_errors.txt");
        cmds.add("adb -s "+ serial+" shell rm /sdcard/powerword/tests_result_errors.txt");
        return cmds;
    }
}
