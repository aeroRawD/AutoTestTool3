package com.att.testcommand;

import java.util.ArrayList;
import java.util.List;

public abstract class TestCommandAdapter implements TestCommand {
    List<String> errorMsgs = new ArrayList<String>();

    public void addErrorMsg(String error) {
        errorMsgs.add(error);
    }

    public String getErrorMsg() {
        String errors = "";
        for (String eror : errorMsgs) {
            errors += eror + "\r\n";
        }
        return errors;
    }

    List<String> shortMsgs = new ArrayList<String>();

    public void addShortMsg(String error) {
        shortMsgs.add(error);
    }

    List<String> longMsgs = new ArrayList<String>();

    public void addLongMsg(String error) {
        longMsgs.add(error);
    }

    public String getShortMsg() {
        String errors = "";
        for (String eror : shortMsgs) {
            errors += eror + "\r\n";
        }
        return errors;
    }

    public String getLongMsg() {
        String errors = "";
        for (String eror : longMsgs) {
            errors += eror + "\r\n";
        }
        return errors;
    }
}
