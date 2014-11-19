package com.att.svn;

import java.util.List;
import java.util.logging.Logger;

import com.log.Log;

/**
 * E:\>svn info D:\data\powerword7 Path: D:\data\powerword7 
 * Working Copy Root
 * Path: D:\data\powerword7 URL:
 * http://trac.s.iciba.com/svn/ciba-mobile/android/Powerword7 Relative URL:
 * ^/android/Powerword7 Repository Root: http://trac.s.iciba.com/svn/ciba-mobile
 * Repository UUID: cd345185-6d80-4e91-b8e5-2361b29444f4 Revision: 15528 Node
 * Kind: directory Schedule: normal Last Changed Author: shaopengxiang Last
 * Changed Rev: 15528 Last Changed Date: 2014-10-29 17:06:13 +0800 (周三, 29 十月
 * 2014)
 */
public class SvnInfo {
	private static Logger logger = Log.getSlientLogger("svn");
	public static SvnInfo getSvnInfo(List<String> content) {
		SvnInfo svnInfo = new SvnInfo();
		for (String s : content) {
			//logger.info("svn info输出:" + s);
			if (s.startsWith("Working Copy Root Path"))
				svnInfo.setWorkingPath(SvnUtil.getValue(s,
						"Working Copy Root Path"));
			if (s.startsWith("URL"))
				svnInfo.setURL(SvnUtil.getValue(s, "URL"));
			if (s.startsWith("Revision"))
				svnInfo.setRevision(SvnUtil.getValue(s, "Revision"));
			if (s.startsWith("Last Changed Author"))
				svnInfo.setLastChangedAuthor(SvnUtil.getValue(s,
						"Last Changed Author"));
			if (s.startsWith("Last Changed Rev"))
				svnInfo.setLastChangedRevId(SvnUtil.getValue(s,
						"Last Changed Rev"));
			if (s.startsWith("Last Changed Date"))
				svnInfo.setLastChangedTime(SvnUtil.getValue(s,
						"Last Changed Date"));
		}

		return svnInfo;
	}

	private String workingPath = "";
	private String URL;
	private String revision;
	private String lastChangedAuthor;
	private String lastChangedRevId;
	private String lastChangedTime;

	public String getWorkingPath() {
		return workingPath;
	}

	public void setWorkingPath(String workingPath) {
		this.workingPath = workingPath;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getRevision() {
		//return revision;
	    return lastChangedRevId;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getLastChangedAuthor() {
		return lastChangedAuthor;
	}

	public void setLastChangedAuthor(String lastChangedAuthor) {
		this.lastChangedAuthor = lastChangedAuthor;
	}

	public String getLastChangedRevId() {
		return lastChangedRevId;
	}

	public void setLastChangedRevId(String lastChangedRevId) {
		this.lastChangedRevId = lastChangedRevId;
	}

	public String getLastChangedTime() {
		return lastChangedTime;
	}

	public void setLastChangedTime(String lastChangedTime) {
		this.lastChangedTime = lastChangedTime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Working Copy Root Path:" + getWorkingPath() + "\r\n");
		sb.append("URL:" + getURL() + "\r\n");
		sb.append("Revision:" + getRevision() + "\r\n");
		sb.append("Last Changed Author:" + getLastChangedAuthor() + "\r\n");
		sb.append("Last Changed Rev:" + getLastChangedRevId() + "\r\n");
		sb.append("Last Changed Date:" + getLastChangedTime() + "\r\n");
		return sb.toString();
	}

}
