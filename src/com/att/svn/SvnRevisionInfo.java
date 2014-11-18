package com.att.svn;

import java.util.List;

import com.log.Log;
import com.spx.adb.Util;

public class SvnRevisionInfo {
    private String revId = "";
    private String author = "";
    private String submitTime = "";
    private String lineCount = "";
    private String revisionContent = "";
    private String revisionInfo = "";
    private String comment = "";
    private String changedPath = "";

    public SvnRevisionInfo(List<String> content) {

        int startLine = 0;
        int endLine = content.size() - 1;
        for (int i = 0; i < content.size(); i++) {
            String s = content.get(i);
            if (s.startsWith("-------------")) {
                startLine = i + 1;
                break;
            }
        }
        for (int i = content.size() - 1; i >= 0; i--) {
            String s = content.get(i);
            if (s.startsWith("-------------")) {
                endLine = i - 1;
                break;
            }
        }
        revisionInfo = content.get(startLine);
        Log.d("revisionInfo:"+revisionInfo);
        parseRevisionInfo();
        comment = content.get(endLine);
        for (int i = startLine + 1; i <endLine; i++) {
            if(!Util.isNull(content.get(i))){
                //Log.d("content.get("+i+").trim():"+content.get(i).trim());
                changedPath += content.get(i).trim() + "\r\n";
            }
        }
        // revisionContent = content;
    }

    private void parseRevisionInfo() {
        if (revisionInfo == null)
            return;

        revisionInfo = revisionInfo.trim();
        String info = revisionInfo;
        if (info.startsWith("revision info:")) {
            info = info.substring(14);
        }
        
        Log.d("info:"+info);
        List<String> parts = Util.split(info, "|");
        Log.d("parts.size:"+parts.size());
        if (parts.size() == 4) {
            revId = parts.get(0);
            author = parts.get(1);
            submitTime = parts.get(2);
            lineCount = parts.get(3);
            Log.d("revId:"+revId);
        }
    }

    public String getRevisionInfo() {
        return revisionInfo;
    }

    public String getRevId() {
        if(revId!=null && revId.startsWith("r")) revId = revId.substring(1);
        return revId;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public String getLineCount() {
        return lineCount;
    }

    public String getRevisionDetail() {
        return revisionInfo.trim() + "\r\n" + changedPath.trim() + "\r\n" + "修改说明: "
                + comment;
    }
}
