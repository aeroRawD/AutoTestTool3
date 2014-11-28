package com.att.build;

import java.util.logging.Logger;

import com.android.ddmlib.MultiLineReceiver;
import com.log.Log;
import com.spx.adb.Util;

public class ConsoleOutputReceiver extends MultiLineReceiver {
    private static Logger logger = Log.getSlientLogger("ConsoleOutputReceiver");
    private boolean failed = false;
    private String errorMsg = "";
    private boolean apkvalid = true;
    private String shortMsg = "";
    private String longMsg = "";
    private StringBuilder testOuput = new StringBuilder();

    public String getOutput() {
        return testOuput.toString();
    }

    public void setCmd(String cmd) {
        testOuput.append("命令:" + cmd + "\r\n");
        testOuput.append("\r\n");
    }

    public String getShortMsg() {
        return shortMsg;
    }

    public String getLongMsg() {
        return longMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isFailed() {
        return failed || !Util.isNull(errorMsg);
    }

    public boolean isApkValid() {
        return apkvalid;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void processNewLines(String[] lines) {
        boolean append = false;
        StringBuilder temp = new StringBuilder();
        for (String line : lines) {
            if (!isIgnoredString(line)) {
                temp.append(line + "\r\n");
                append = true;
                logger.info(line);
            }

            if (line.contains("FAILURES!!!")) {
                failed = true;
            } else if (line.startsWith("junit.framework.AssertionFailedError:")) {
                errorMsg = line
                        .substring("junit.framework.AssertionFailedError:"
                                .length());
            }

            if (line.contains("INSTRUMENTATION_RESULT:")
                    && (line.contains("java.lang.RuntimeException") || line
                            .contains("crashed"))) {
                failed = true;
            }

            if (line.contains("INSTRUMENTATION_FAILED")
                    || line.contains("INSTRUMENTATION_STATUS: Error")) {
                failed = true;
            }

            if (line.startsWith("INSTRUMENTATION_RESULT: longMsg=")) {
                longMsg = line.substring("INSTRUMENTATION_RESULT: longMsg="
                        .length());
            }

            if (line.startsWith("INSTRUMENTATION_RESULT: shortMsg=")) {
                shortMsg = line.substring("INSTRUMENTATION_RESULT: shortMsg="
                        .length());
            }

            if (line.startsWith("INSTRUMENTATION_STATUS: Error=")) {
                errorMsg = line.substring("INSTRUMENTATION_STATUS: Error="
                        .length());
            }

            if (line.contains("Could not find test class")) {
                apkvalid = false;
            }
        }

        if (append) {
            testOuput.append(Util.getTimeStr("yyyy-MM-dd HH:mm:ss") + ":\r\n");
            testOuput.append(temp.toString());
        }
    }

    private boolean isIgnoredString(String line) {
        if (Util.isNull(line) || line.startsWith("BT INFO: 2")) {
            return true;
        }
        return false;
    }

    public void reset() {
        failed = false;
        errorMsg = "";
        apkvalid = true;
        shortMsg = "";
        longMsg = "";
        testOuput = new StringBuilder();
    }
}
