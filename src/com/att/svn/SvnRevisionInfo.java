package com.att.svn;

import java.util.List;

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
		int endLine = content.size()-1;
		for(int i=0;i<content.size();i++){
			String s = content.get(i);
			if(s.startsWith("-------------")){
				startLine = i+1;
				break;
			}
		}
		for(int i=content.size()-1;i>=0;i--){
			String s = content.get(i);
			if(s.startsWith("-------------")){
				endLine = i-1;
				break;
			}
		}
		revisionInfo = content.get(startLine);
		parseRevisionInfo();
		comment = content.get(endLine);
		for(int i=startLine+1;i<endLine;i++){
			changedPath+=content.get(i)+"\r\n";
		}
//		revisionContent = content;
	}
	
	private void parseRevisionInfo(){
	    if(revisionInfo==null)
	        return;
	    
	    revisionInfo = revisionInfo.trim();
	    String info = revisionInfo;
	    if(info.startsWith("revision info:")){
	        info = info.substring(14);
	    }
	    
	    String[] parts = info.split("|");
	    if(parts.length==4){
	        revId = parts[0].trim();
	        author = parts[1].trim();
	        submitTime = parts[2].trim();
	        lineCount = parts[3].trim();
	    }
	}

	
	
	public String getRevisionInfo() {
        return revisionInfo;
    }

    public String getRevId() {
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
		return revisionInfo+"\r\n"+changedPath+"\r\n"+comment;
	}
}
