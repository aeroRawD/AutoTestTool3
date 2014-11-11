package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;

public class TestWifiOnCommand implements TestCommand{

    @Override
    public List<String> getBeforeCommands(String serial) {
        List<String> cmds = new ArrayList<String>();
        cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.openwifi");
        cmds.add("adb -s "+ serial+" shell am broadcast -a kingsoft.test.cmd.action.openwifi");
        return cmds;
    }

    @Override
    public String getCommand(){
        return "am instrument -e wifi on -w com.kingsoft.test/com.kingsoft.test.framework.UseCaseTestRunner";
    }

    @Override
    public List<String> getAfterCommands(String serial) {
        // TODO Auto-generated method stub
        return null;
    }

}
