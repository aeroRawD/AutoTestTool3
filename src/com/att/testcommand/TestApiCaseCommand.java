package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;


public class TestApiCaseCommand implements TestCommand{

    @Override
    public List<String> getBeforeCommands(String serial) {
        List<String> cmds = new ArrayList<String>();
        return cmds;
    }

    
    public String getCommand(){
        return "am instrument -w -e class com.kingsoft.test.api.ApiTest#testApi com.kingsoft.test/android.test.InstrumentationTestRunner";
    }


    @Override
    public List<String> getAfterCommands(String serial) {
        // TODO Auto-generated method stub
        return null;
    }

}
