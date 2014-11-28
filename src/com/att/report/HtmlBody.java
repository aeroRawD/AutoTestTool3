package com.att.report;


import java.util.List;
import java.util.Map;

import com.spx.adb.Util;

public class HtmlBody {
    String result = "";
    String bodyCode;
    List<UseCase> useCaseList = null;
    List<UseCase> failedUseCaseList = null;
    List<UseCase> succeedUseCaseList = null;
    
    MailContentBuilder mailBuilder =null;
    TestInfo testingInfo = null;
    boolean dailyReport = false;
    public HtmlBody(MailContentBuilder mailBuilder, String str, List<UseCase> useCaseList,List<UseCase> failedList,List<UseCase> succeedList, TestInfo testingInfo, boolean dailyReport) {
        bodyCode = str;
        this.useCaseList = useCaseList;
        this.failedUseCaseList = failedList;
        this.succeedUseCaseList = succeedList;
        this.mailBuilder = mailBuilder;
        this.testingInfo = testingInfo;
        this.dailyReport = dailyReport;
        parseCode();
    }

    /**
     * 解析html模板中的变量
     */
    public void parseCode() {
        StringBuilder sb = new StringBuilder();
        
        

        String replacedHtml = testingInfo.replaceTags(bodyCode);
        sb.append(replacedHtml);
        System.out.println("replacedHtml:" + replacedHtml);
        
        String beginTag="<div name=\"fail_case\">";
        String endTag ="</div>";
        result = MailContentBuilder.replaceHtmlPart(sb.toString(), beginTag, endTag, getFailedTableContent());
        
        
        beginTag="<div name=\"pass_case\">";
        endTag ="</div>";
        result = MailContentBuilder.replaceHtmlPart(result, beginTag, endTag, getPassedTableContent());
        
    }

    private String getPassedTableContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"wb\">\r\n");

        // 标题栏
        sb.append("<tr>\r\n");
        sb.append("<th class=\"wb\">ID</th>\r\n");
        sb.append("<th class=\"wb\">用例标题</th>\r\n");
        sb.append("<th class=\"wb\">代码方法</th>\r\n");
        sb.append("<th class=\"wb\"></th>\r\n");
        sb.append("<th class=\"wb\">wifi</th>\r\n");
        sb.append("<th class=\"wb\">负责人</th>\r\n");
        
        sb.append("</tr>\r\n");

        // List<UseCase> useCaseList =
        // testResultBuilder.getParesedUsecaseResult();
        System.out.println("succeedUseCaseList:" + succeedUseCaseList);
        System.out.println("succeedUseCaseList.size:" + succeedUseCaseList.size());

        //生成失败用例报告
        for (int i = 0; i < succeedUseCaseList.size(); i++) {
            UseCase uc = succeedUseCaseList.get(i);
            if (uc == null || !uc.isPassed()) {
                continue;
            }

            sb.append("<tr>\r\n");
            sb.append("<td class=\"PA\">" + uc.getAttr("caseid") + "</td>\r\n");
            sb.append("<td class=\"PA\">" + uc.getAttr("title") + "</td>\r\n");
            sb.append("<td class=\"PA\">" + uc.getAttr("classname") + "."
                    + uc.getAttr("name") + "</td>\r\n");
            sb.append("<td class=\"PA\">PASS</td>\r\n");
            sb.append("<td class=\"PA\">" + uc.getAttr("wifi") + "</td>\r\n");
            sb.append("<td class=\"PA\">" + uc.getAttr("author") + "</td>\r\n");
            
            sb.append("</tr>\r\n");
        }

        sb.append("<table>\r\n");
        return sb.toString();
    }

    private String getFailedTableContent() {
        
        // 解析12个小时内的测试数据, 得到一个map数据结构
        Map<String, Integer> failedData = HisotoryDataCollector.getFailedTestCaseData(testingInfo.getSerial(), 10);
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"wb\">\r\n");

        // 标题栏
        sb.append("<tr>\r\n");
        sb.append("<th class=\"wb\">ID</th>\r\n");
        sb.append("<th class=\"wb\">用例标题</th>\r\n");
        sb.append("<th class=\"wb\">代码方法</th>\r\n");
        sb.append("<th class=\"wb\">告警提示</th>\r\n");
        sb.append("<th class=\"wb\">wifi</th>\r\n");
        sb.append("<th class=\"wb\">负责人</th>\r\n");
        sb.append("<th class=\"wb\">最近失败次数</th>\r\n");
        sb.append("</tr>\r\n");

        // List<UseCase> useCaseList =
        // testResultBuilder.getParesedUsecaseResult();
//        System.out.println("mUseCaseList:" + useCaseList);
//        System.out.println("mUseCaseList.size:" + useCaseList.size());

        //生成失败用例报告
        for (int i = 0; i < failedUseCaseList.size(); i++) {
            UseCase uc = failedUseCaseList.get(i);
            if (uc == null || uc.isPassed()) {
                continue;
            }
            String test = uc.getAttr("classname") + "." + uc.getAttr("name");
            sb.append("<tr>\r\n");
            sb.append("<td class=\"wb\">" + uc.getAttr("caseid") + "</td>\r\n");
            sb.append("<td class=\"wb\">" + uc.getAttr("title") + "</td>\r\n");
            sb.append("<td class=\"wb\">" + test+ "</td>\r\n");
            String error = uc.getAttr("error");
            if(Util.isNull(error) || "NULL".equalsIgnoreCase(error)|| "no error".equalsIgnoreCase(error)){
                error = uc.getError();
            }
            sb.append("<td class=\"wb\">" + error + "</td>\r\n");
            sb.append("<td class=\"wb\">" + uc.getAttr("wifi") + "</td>\r\n");
            sb.append("<td class=\"wb\">" + uc.getAttr("author") + "</td>\r\n");
            String failTimes = "";
            if (failedData.get(test) != null) {
                failTimes = failedData.get(test)+"";
            }
            sb.append("<td class=\"PA\"> "+failTimes+"</td>\r\n");
            sb.append("</tr>\r\n");
        }

        sb.append("<table>\r\n");
        return sb.toString();
    }

    public String getHtml() {
        return result;
    }
}
