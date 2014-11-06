package com.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


public class TxtLoggerFormater extends Formatter {

    private final Date dat = new Date();

	@Override
    public String format(LogRecord rec) {

		StringBuffer buf = new StringBuffer(1000);
		buf.append(calcTime(rec.getMillis())+" ");
		if (rec.getSourceMethodName() != null) {
			buf.append(rec.getSourceClassName()+"."+rec.getSourceMethodName()+"\t");
         }
		
		if (rec.getThrown() != null) {
			String throwable="";
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            rec.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
            buf.append("\t"+throwable);
        }
		buf.append("\t" + formatMessage(rec)+"\r\n");
//		buf.append("\r\n");

		return buf.toString();
		

	}

	private String calcTime(long millisecs) {

		SimpleDateFormat date_format = new SimpleDateFormat("MM-dd HH:mm:ss:SSS");

		Date resultdate = new Date(millisecs);

		return date_format.format(resultdate);

	}

}
