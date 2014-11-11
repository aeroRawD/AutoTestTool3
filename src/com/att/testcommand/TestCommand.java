package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;

public interface TestCommand {
    public List<String> getBeforeCommands(String serial);
    public String getCommand();
    public List<String> getAfterCommands(String serial);
}
