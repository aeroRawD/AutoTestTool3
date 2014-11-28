package com.att.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;

import com.att.report.HisotoryDataCollector;
import com.chart.A3BarChartCreator;
import com.spx.adb.Util;

public class UseCaseDataGenerator {

    private static UseCaseDataGenerator sInstance = new UseCaseDataGenerator();

    private UseCaseDataGenerator() {
    };

    public static UseCaseDataGenerator getInstance() {
        return sInstance;
    }

    public List<UseCaseRunResult> getLastTestResult(String serial, int count) {
        String path = "data/backup/workspace/" + serial;
        List<String> lastFileNames = HisotoryDataCollector.getLastFileNames(path, null, count);
//        File workspace = new File(path);
//        String[] subpaths = workspace.list();
//        System.out.println("subpaths:"+subpaths);
//        if (subpaths == null)
//            return null;
//        
//        System.out.println("subpaths.length:"+subpaths.length);
//
//        Arrays.sort(subpaths);
//        List<UseCaseRunResult> results = new ArrayList<UseCaseRunResult>();
//        
//        int c = 0;
//        int start = subpaths.length-count;
//        if(start <0) start =0;
//        for (int i = start; c < count && i <subpaths.length; i++,c++) {
//            UseCaseRunResult ucr = new UseCaseRunResult(path + "/"
//                    + subpaths[i]+"/final.txt");
//            results.add(ucr);
//        }

        List<UseCaseRunResult> results = new ArrayList<UseCaseRunResult>();
        for(int i=0;i<lastFileNames.size();i++){
            UseCaseRunResult ucr = new UseCaseRunResult(path + "/"
                  + lastFileNames.get(i)+"/final.txt");
          results.add(ucr);
        }
        
        return results;
    }
    
    public void createBarChart(String serial, double[][] data, String[] titles_Y, String savePath){
        System.out.println("data:"+data);
        System.out.println("titles_Y:"+titles_Y);
        for(int i=0;i<data.length;i++){
            for(int j=0;j<data[i].length;j++){
                System.out.println("data["+i+"]["+j+"]:"+data[i][j]);
            }
        }
        
        for(int i=0;i<titles_Y.length;i++){
            System.out.println("titles_Y["+i+"]:"+titles_Y[i]);
        }
        // 步骤1：创建CategoryDataset对象（准备数据）
        CategoryDataset dataset = A3BarChartCreator.createDoubleBarDataset(data, titles_Y);
        // 步骤2：根据Dataset 生成JFreeChart对象，以及做相应的设置
        JFreeChart freeChart = A3BarChartCreator.createChart("用例执行数据统计","日期","数目", true, dataset);
        // 步骤3：将JFreeChart对象输出到文件，Servlet输出流等
        //String time = Util.getTimeStr("yyyy-MM-dd_HHmmss");
        //A3BarChartCreator.saveAsFile(freeChart, "data/img/"+serial+"/bar_"+time+".png", 800, 400);
        A3BarChartCreator.saveAsFile(freeChart, savePath, 800, 400);
    }
    
    public static void main(String[] args){
        List<UseCaseRunResult> lastTestResult = UseCaseDataGenerator
                .getInstance().getLastTestResult("0149C6F415019008", 32);
        for(int i=0;i<lastTestResult.size();i++){
            UseCaseRunResult useCaseRunResult = lastTestResult.get(i);
            System.out.println("name:"+useCaseRunResult.getName());
        }
    }
}
