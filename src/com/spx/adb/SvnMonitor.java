package com.spx.adb;

import java.util.ArrayList;
import java.util.List;

import com.att.svn.SvnUpdateListener;

public class SvnMonitor extends Thread {

	private SvnUpdateListener listener = null;

	// 被监控的SVN url列表
	private List<String> watchedUrls = new ArrayList<String>();

	private static SvnMonitor sInstance = new SvnMonitor();

	private SvnMonitor() {
	};

	public static SvnMonitor getInstance() {
		return sInstance;
	}

	public void run() {
		while (true) {
			checkSvnUpdate();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 检查SVN是否更新
	 */
	private void checkSvnUpdate() {

	}

	private void notifySvnListeners(String url, String updatedContent,
			String revId) {
		if (listener != null) {
			listener.onSvnUpdate(url, updatedContent, revId);
		}
	}

	public SvnUpdateListener getListener() {
		return listener;
	}

	public void setListener(SvnUpdateListener listener) {
		this.listener = listener;
	}

	public void addWatchedUrl(String url) {
		watchedUrls.add(url);
	}
}
