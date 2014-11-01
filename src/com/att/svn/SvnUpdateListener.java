package com.att.svn;

public interface SvnUpdateListener {
	public void onSvnUpdate(String url, String updatedContent, String revId);
}
