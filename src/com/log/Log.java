package com.log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Log {
	// get the global logger to configure it
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
//	private static Logger logger = null; 
//	static {
//		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
//
//	    // suppress the logging output to the console
//
//	    Logger rootLogger = Logger.getLogger("");
//
//	    Handler[] handlers = rootLogger.getHandlers();
//
//	    if (handlers[0] instanceof ConsoleHandler) {
//	      rootLogger.removeHandler(handlers[0]);
//	      handlers[0].setFormatter(new TxtLoggerFormater());
//	    }

//		Handler[] handlers = logger.getHandlers();
//		System.out.println("handlers of root:"+handlers);
//		if(handlers!=null){
//			for(Handler handler:handlers){
//				System.out.println("handler:"+handler);
//			}
//		}
//		if (handlers != null && handlers.length > 0
//				&& handlers[0] instanceof ConsoleHandler) {
//			// logger.removeHandler(handlers[0]);
//			handlers[0].setFormatter(new TxtLoggerFormater());
//		}
//	}
	private static HashMap<String, Logger> loggerMap = new HashMap<String, Logger>();
	private static HashMap<String, Logger> silentLoggerMap = new HashMap<String, Logger>();
	
	public static void setup(){
		
	}

	private static Logger createLogger(String tag) {
		Logger logger = Logger.getLogger(tag);
		Handler[] handlers = logger.getHandlers();
		System.out.println("handlers of "+tag+":"+handlers);
		if(handlers!=null){
			for(Handler handler:handlers){
				System.out.println("handler:"+handler);
			}
		}
		if (handlers != null && handlers.length > 0
				&& handlers[0] instanceof ConsoleHandler) {
			// logger.removeHandler(handlers[0]);
			handlers[0].setFormatter(new TxtLoggerFormater());
		}
		return logger;
	}

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static void d(String str) {
		//System.out.println((sdf.format(new Date()))+"\t"+str.trim());
		logger.log(Level.INFO, str);
	}

	public static void d(String tag, String str) {
		// System.out.println((sdf.format(new Date()))+"\t"+tag+"\t"+str);
		if (loggerMap.get(tag) == null) {
			loggerMap.put(tag, createLogger(tag));
		}
		loggerMap.get(tag).log(Level.INFO, str);
	}

	public static void e(String str) {
		logger.log(Level.SEVERE, str);
	}

	public static void e(String tag, String str) {
		if (loggerMap.get(tag) == null) {
			loggerMap.put(tag, createLogger(tag));
		}
		loggerMap.get(tag).log(Level.SEVERE, str);
	}

	public static Logger getSlientLogger(String name) {
		if (silentLoggerMap.get(name) == null) {
			Logger logger = Logger.getLogger(name);
//			Handler[] handlers = logger.getHandlers();
//			System.out.println("handlers of "+name+":"+handlers);
//			if(handlers!=null){
//				for(Handler handler:handlers){
//					System.out.println("handler:"+handler);
//				}
//			}
//			if (handlers != null && handlers.length > 0
//					&& handlers[0] instanceof ConsoleHandler) {
//				//logger.removeHandler(handlers[0]);
//				handlers[0].setFormatter(new TxtLoggerFormater());
//			} else {
//				ConsoleHandler mycosoleHandler = new ConsoleHandler();
//				mycosoleHandler.setFormatter(new TxtLoggerFormater());
//				logger.addHandler(mycosoleHandler);
//			}
//			try {
//				FileHandler fileTxt = new FileHandler("log/" + name
//						+ sdf.format(new Date()));
//				fileTxt.setFormatter(new TxtLoggerFormater());
//				logger.addHandler(fileTxt);
//				
//			} catch (SecurityException | IOException e) {
//				e.printStackTrace();
//			}

			silentLoggerMap.put(name, logger);
		}

		return silentLoggerMap.get(name);
	}
}
