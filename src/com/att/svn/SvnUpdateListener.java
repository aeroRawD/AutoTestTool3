package com.att.svn;

public interface SvnUpdateListener {
	public void onSvnUpdate(String url, SvnRevisionInfo revisionInfo, String localpath);
}
