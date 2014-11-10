package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;


public class TestWifiOffCommand implements TestCommand{

   public List<String> getPreCommands(){
       List<String> cmds = new ArrayList<String>();
       cmds.add("am broadcast -a kingsoft.test.cmd.action.closewifi");
       cmds.add("am broadcast -a kingsoft.test.cmd.action.closenetwork");
       return cmds;
   }

   
   public String getCommand(){
       return "am instrument -e wifi off -w com.kingsoft.test/com.kingsoft.test.framework.UseCaseTestRunner";
   }
}
