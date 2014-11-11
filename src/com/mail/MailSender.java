package com.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.log.Log;
import com.spx.adb.SystemEnv;
import com.spx.adb.Util;
import com.sun.mail.smtp.SMTPMessage;

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
    
    private HashMap<String, Long> notifyDataMap = new HashMap<String, Long>();
    /**
     * 发送编译不过的邮件提醒
     */
    public void sendBuildFailedNotify(String url, String buildError){
        if(notifyDataMap.get(url)!=null){
            long lastNotifyTime = notifyDataMap.get(url);
            if(System.currentTimeMillis()- lastNotifyTime < 60*60*1000){
                logger.info("alert mail already sent nearby.");
                return;
            }
        }
        sendMail(MailSender.defaultRecipients, "[自动邮件]编译失败呀, 请检查"+url+"上代码是否正常.", buildError.toString(), false);
        notifyDataMap.put(url, System.currentTimeMillis());
    }

    public void sendTxtMail(List<String> recipients, String subject,
            String mailContent) {
        logger.info("will send text mail...");

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
    
    public void sendMail(List<String> recipients, String subject,
            String textContent, boolean useHtml){
        if(useHtml){
//            StringBuilder html = new StringBuilder();
//            html.append("<html><head><title>This is not usually displayed</title></head>");
//            html.append("<body><div>");
//            html.append(""+textContent);
//            html.append("</div></body></html>");
//            sendMessageWithEmbededImage(recipients, subject, html.toString());
            sendTxtMail(recipients, subject, textContent);
        }else{
            sendTxtMail(recipients, subject, textContent);
        }
    }
    
    private void sendHtmlMail(List<String> recipients, String subject,
            String htmlMailContent){
        logger.info("will send html mail...");

        Session session = Session.getInstance(properties, authenticator);

        try{
            
            
           // Create a default MimeMessage object.
           MimeMessage message = new MimeMessage(session);

           // Set From: header field of the header.
           message.setFrom(new InternetAddress(from));

           for (String recip : recipients) {
               message.addRecipient(Message.RecipientType.TO,
                       new InternetAddress(recip));
           }

           // Set Subject: header field
           message.setSubject(subject);
           

           // Send the actual HTML message, as big as you like
           message.setContent(htmlMailContent,
                              "text/html; charset=\"UTF-8\"" );

           // Send message
           Transport.send(message);
           System.out.println("Sent html message successfully....");
        }catch (MessagingException mex) {
           mex.printStackTrace();
        }
    }
    
    public void sendMessageWithEmbededImage(List<String> recipients, String subject,
            String htmlMailContent) {
        //Session session = Session.getInstance(properties, authenticator);
        Session session = buildGoogleSession();
        try {
            Message withImage = buildMessageWithEmbeddedImage(session);
            for (String recip : recipients) {
                withImage.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(recip));
            }
            Transport.send(withImage);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("send html mail failed!" + ex.getMessage());
        }
    }
    
    /**
     * Send the message with Transport.send(Message)
     * 
     * @param message
     * @param recipient
     * @throws MessagingException
     */
    public static void addressAndSendMessage(Message message, String recipient)
        throws AddressException, MessagingException {
      message.setRecipient(RecipientType.TO, new InternetAddress(recipient));
      Transport.send(message);
    }
    
    /**
     * Build an HTML message with an image embedded in the message.
     * 
     * @param session
     * @return a multipart MIME message where the main part is an HTML message and the 
     * second part is an image that will be displayed within the HTML.
     * @throws MessagingException
     * @throws IOException
     */
    public static Message buildMessageWithEmbeddedImage(Session session)
        throws MessagingException, IOException {
      SMTPMessage m = new SMTPMessage(session);
      MimeMultipart content = new MimeMultipart("related");
      
      // ContentID is used by both parts
      String cid = ContentIdGenerator.getContentId();
      
      // HTML part
      MimeBodyPart textPart = new MimeBodyPart();
      textPart.setText("<html><head>"
        + "<title>This is not usually displayed</title>"
        + "</head>\n"
        + "<body><div><b>Hi there!</b></div>"
        + "<div>Sending HTML in email is so <i>cool!</i> </div>\n"
        + "<div>And here's an image: <img src=\"cid:"
        + cid
        + "\" /></div>\n" + "<div>I hope you like it!</div></body></html>", 
        "US-ASCII", "html");
      content.addBodyPart(textPart);

      // Image part
      MimeBodyPart imagePart = new MimeBodyPart();
      imagePart.attachFile("data/myimg.gif");
      imagePart.setContentID("<" + cid + ">");
      imagePart.setDisposition(MimeBodyPart.INLINE);
      content.addBodyPart(imagePart);
      
      m.setContent(content);
      m.setSubject("Demo HTML message");
      return m;
    }
    
    /**
     * Build a Session object for an SMTP server that requires both TSL and
     * authentication. This uses Gmail as an example of such a server
     * 
     * @return a Session for sending email
     */
    public static Session buildGoogleSession() {
      Properties mailProps = new Properties();
      mailProps.put("mail.transport.protocol", "smtp");
      mailProps.put("mail.host", host);
      mailProps.put("mail.from", "shaopengxiang@kingsoft.com");
      mailProps.put("mail.smtp.starttls.enable", "true");
      mailProps.put("mail.smtp.port", "587");
      mailProps.put("mail.smtp.auth", "true");
      // final, because we're using it in the closure below

      Session session = Session.getInstance(mailProps, authenticator);
      session.setDebug(false);
      return session;

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
    
    public static void createImg(){
        
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        MailSender.getInstance().sendMail(MailSender.defaultRecipients,
                "hahah", "yaya", true);
    }

}
