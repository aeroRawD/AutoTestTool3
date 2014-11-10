package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;

public interface TestCommand {
    public List<String> getPreCommands();
    public String getCommand();
}
