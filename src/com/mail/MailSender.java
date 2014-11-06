package com.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.log.Log;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;

public class MailSender {
    private Logger logger = Log.getSlientLogger("MailSender");

    private static final String to = SystemEnv.admin;
    // Assuming you are sending email from localhost
    // SMTP server ip
    private static final String host = "114.255.44.145";
    // private static final String TESTREPORT_PATH = "testreport";
    // private static final String MEMINFO_FILE_NAME = "meminfo.txt";
    // private static final String PROP_FILE_NAME = "prop.txt";

    // Get system properties
    private static Properties properties = System.getProperties();
    private static Authenticator authenticator = new Authenticator();
    // Setup mail server
    static {
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", "587");

        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.submitter", authenticator
                .getPasswordAuthentication().getUserName());
    }
    private static final String from = "shaopengxiang@kingsoft.com";

    public static List<String> defaultRecipients = new ArrayList<String>();
    static {
        defaultRecipients.add(to);
    }

    private static MailSender sInstance = new MailSender();

    private MailSender() {
    }

    public static MailSender getInstance() {
        return sInstance;
    }

    public void sendTxtMail(List<String> recipients, String subject,
            String mailContent) {
        // Get the default Session object.
        // Session session = Session.getDefaultInstance(properties);

        Session session = Session.getInstance(properties, authenticator);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            for (String recip : recipients) {
                message.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(recip));
            }

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(mailContent.toString());

            // SmtpClient client = new SmtpClient();
            //
            // client.Credentials = new NetworkCredential("mymailid",
            // "mypassword", "smtp.gmail.com");
            // client.Host = "smtp.gmail.com";
            // client.Port = 587;
            // client.DeliveryMethod = SmtpDeliveryMethod.Network;
            // client.EnableSsl = true;
            // client.UseDefaultCredentials = true;
            // Transport transport = session.getTransport("smtp");
            // transport.connect(host, 587, " ", " ");

            // Send message
            Transport.send(message);
            System.out.println("Sent text message successfully....");
            logger.info("Sent text message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
            logger.severe("Sent text message failed!!!");
        }
    }

    private static class Authenticator extends javax.mail.Authenticator {
        private PasswordAuthentication authentication;

        public Authenticator() {
            List<String> fileContentLines = Util
                    .getFileContentLines("local.properties");
            String username = "shaopengxiang";
            String password = "spx";
            for (String line : fileContentLines) {
                if (line.startsWith("mail.user=")) {
                    username = line.substring("mail.user=".length());
                }
                if (line.startsWith("mail.user.password=")) {
                    password = line.substring("mail.user.password=".length());
                }
            }

            authentication = new PasswordAuthentication(username, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return authentication;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        MailSender.getInstance().sendTxtMail(MailSender.defaultRecipients,
                "hahah", "yaya");
    }

}
