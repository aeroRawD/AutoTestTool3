package com.att.svn;

import java.util.List;

public class SvnRevisionInfo {
	private String revisionContent = "";
	private String summary = "";
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
		summary = content.get(startLine);
		comment = content.get(endLine);
		for(int i=startLine+1;i<endLine;i++){
			changedPath+=content.get(i)+"\r\n";
		}
//		revisionContent = content;
	}

	public String getRevisionDetail() {
		return summary+"\r\n"+changedPath+"\r\n"+comment;
	}
}
