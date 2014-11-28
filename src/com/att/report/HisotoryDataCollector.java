package com.att.report;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.att.Constant;

/**
 * 负责历史数据的获取
 * @author shaopengxiang
 *
 */
public class HisotoryDataCollector {

    public static Map<String, Integer> getFailedTestCaseData(String serial,
            int round) {
        Map<String, Integer> data = new HashMap<String, Integer>();
        List<HistoryTestResult> historyTestResults = getHistoryTestResults(serial, round);
        for(int i=0;i<historyTestResults.size();i++){
            HistoryTestResult historyTestResult = historyTestResults.get(i);
            List<String> failedTestCases = historyTestResult.getFailedTestCases();
            for(int j=0;j<failedTestCases.size();j++){
                String test = failedTestCases.get(j);
                if(data.get(test)==null){
                    data.put(test, 1);
                }else{
                    data.put(test, data.get(test)+1);
                }
            }
        }
        return data;
    }

    public static List<HistoryTestResult> getHistoryTestResults(String serial,
            int round) {
        List<HistoryTestResult> results = new ArrayList<HistoryTestResult>();
        String parentDir = Constant.FULLTEST_RESULT_WORKSPACE + "/" + serial
                + "";
        List<String> lastFileNames = getLastFileNames(parentDir, null, round);

        for (int i = 0; i < lastFileNames.size(); i++) {
            String name = lastFileNames.get(i);
            String fullPath = parentDir + "/" + name;
            HistoryTestResult testResult = new HistoryTestResult(serial,
                    fullPath);
            results.add(testResult);
        }

        return results;
    }

    public static List<String> getLastFileNames(String parentPath,
            final String startFolder, int count) {
        List<String> files = new ArrayList<String>();
        if (count <= 0)
            return files;

        File parentDir = new File(parentPath);
        String[] subpaths = parentDir.list(new java.io.FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (startFolder == null)
                    return true;
                if (name.compareTo(startFolder) < 0)
                    return true;
                return false;
            }
        });
        System.out.println("subpaths:" + subpaths);

        if (subpaths == null || subpaths.length == 0)
            return files;

        Arrays.sort(subpaths);

        int startIndex = subpaths.length - count;
        if (startIndex < 0)
            startIndex = 0;
        int c = 0;
        for (int i = startIndex; c < count && i < subpaths.length; i++, c++) {
            files.add(subpaths[i]);
        }

        return files;
    }

    public static void main(String[] args) {
        List<String> lastFileNames = HisotoryDataCollector.getLastFileNames(
                "data/backup/workspace/2008edd8f316", "2014-11-27_070035", 3);
        System.out.println("lastFileNames.size:" + lastFileNames.size());
        System.out.println("lastFileNames:" + lastFileNames.toString());
        
        Map<String, Integer> failedTestCaseData = getFailedTestCaseData("2008edd8f316", 10);
        System.out.println("failedTestCaseData.size():" + failedTestCaseData.size());
        
        System.out.println("times:" + failedTestCaseData.get("com.kingsoft.fragment.VoaTest.testNoNetArticle"));
    }
}
