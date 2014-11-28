package com.att.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.spx.adb.Util;

public class UseCaseRunResult {

    private List<String> result = new ArrayList<String>();
    private String name = "";

    public UseCaseRunResult(String finalTextFile) {
        result = Util.getFileContentLines(finalTextFile);
        name = finalTextFile;
    }

    public String getName() {
        return name;
    }

    public String getValue(String key) {
        if(result == null) return null;
        
        for (int i = 0; i < result.size(); i++) {
            String line = result.get(i);
            if (line.startsWith(key) && line.indexOf(":") != -1) {
                return line.substring(line.indexOf(":") + 1);
            }
        }
        return "";
    }
}
