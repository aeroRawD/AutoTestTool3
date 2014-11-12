package com.att.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import com.spx.adb.Util;

public class MailContentBuilder {

    private String templeteFileContent = null;
    private HtmlHead mHead = null;
    private HtmlBody mBody = null;
    private TestInfo testingInfo = null;
    private List<UseCase> useCaseList = null;
    private List<UseCase> failedUseCaseList = null;
    private List<UseCase> succeedUseCaseList = null;
    public MailContentBuilder(String templeteFileContent, List<UseCase> useCaseList,List<UseCase> failedList,List<UseCase> succeedList,
            TestInfo testingInfo) {
        this.testingInfo = testingInfo;
        this.templeteFileContent = templeteFileContent;
        this.useCaseList = useCaseList;
        this.failedUseCaseList = failedList;
        this.succeedUseCaseList = succeedList;
    }

    public void build() {

        String headPart = getHtmlPart(templeteFileContent, "<head>", "</head>");
        mHead = new HtmlHead(headPart);
        String bodyPart = getHtmlPart(templeteFileContent, "<body>", "</body>");
        mBody = new HtmlBody(this, bodyPart, useCaseList, failedUseCaseList, succeedUseCaseList, testingInfo);
    }

    public static String getHtmlPart(String html, String beginTag, String endTag) {
        if (html == null || !html.contains(beginTag) || !html.contains(endTag))
            return null;

        int startIndex = html.indexOf(beginTag);
        int endIndex = html.indexOf(endTag) + endTag.length();
        return html.substring(startIndex, endIndex);
    }

    public static String replaceHtmlPart(String html, String beginTag,
            String endTag, String newText) {
        if (html == null || !html.contains(beginTag) || !html.contains(endTag))
            return null;

        int startIndex = html.indexOf(beginTag);
        int endIndex = html.indexOf(endTag) + endTag.length();
        return html.substring(0, startIndex) + newText
                + html.substring(endIndex);
    }

    /**
     * 返回html格式的邮件内容
     * @return
     */
    public String buildHtmlMailContent() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");

        sb.append(mHead.toString());

        sb.append(mBody.getHtml());

        sb.append("</html>");

        try {
            // PrintWriter pw = new PrintWriter(new File(
            // htmlTemplate.getParentFile(), "output.html"));
            PrintWriter pw = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(new File(
                            "output.html")),
                            "UTF-8")));
            pw.write(sb.toString());
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
