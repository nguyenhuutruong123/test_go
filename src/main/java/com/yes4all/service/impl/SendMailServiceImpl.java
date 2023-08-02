package com.yes4all.service.impl;

import com.yes4all.common.errors.BusinessException;
import com.yes4all.common.utils.CommonDataUtil;
import com.yes4all.service.SendMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;


import java.io.UnsupportedEncodingException;


import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.yes4all.constants.MailConstant.*;

@Service
public class SendMailServiceImpl implements SendMailService {

    private static final Logger log = LoggerFactory.getLogger(SendMailServiceImpl.class);
    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.port}")
    private String port;
    @Value("${spring.mail.smtp.auth}")
    private String auth;
    @Value("${spring.mail.smtp.starttls.enable}")
    private String tlsStart;
    @Value("${spring.mail.smtp.ssl.trust}")
    private String sslTrust;
    @Value("${spring.mail.sender}")
    private String sender;

    @Value("${spring.mail.name}")
    private String name;

    @Value("${attribute.env}")
    private String env;

    private void doSendMail(String subject, String content, List<String> receivers,
                            List<String> listMailCC, List<String> listMailBCC, Map<String, String> attachments) {

        log.info("START send mail subject: {}", subject);
        Properties props = new Properties();
        props.put(MAIL_CONFIG_AUTH, auth);
        props.put(MAIL_CONFIG_TLS_START, tlsStart);
        props.put(MAIL_CONFIG_HOST, host);
        props.put(MAIL_CONFIG_PORT, port);
        props.put(MAIL_CONFIG_SSL_TRUST, sslTrust);
        props.setProperty("mail.store.protocol", "imaps");

        Session session = Session.getInstance(props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        try {
            if(!env.equals("PROD")){
                subject="["+env+"]"+subject;
            }

            InternetAddress senderAddress = new InternetAddress(sender, name);

            Message message = new MimeMessage(session);
            message.setFrom(senderAddress);
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", username,
                password);//change the user and password accordingly
            Folder folder = store.getFolder("[Gmail]/Sent Mail");
            folder.open(Folder.READ_WRITE);
            SearchTerm subjectTerm = new SubjectTerm(subject);
            Message[] messages = folder.search(subjectTerm);
            if (messages.length>0) {
                Message messageOld=messages[0];
                    // Get all the information from the message
                    String to = InternetAddress.toString(messageOld
                        .getRecipients(Message.RecipientType.TO));
                    String subject1 = messageOld.getSubject();
                    if (subject1 != null && subject1.equals(subject)) {
                        Message replyMessage = new MimeMessage(session);
                        replyMessage = (MimeMessage) messageOld.reply(false);
                        InternetAddress senderAddressReplay = new InternetAddress(to, name);
                        replyMessage.setFrom(senderAddressReplay);
                        replyMessage.setText(content);
                        for (String toEmail : receivers) {
                            replyMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                        }
                        if (listMailCC != null) {
                            listMailCC.removeAll(receivers);
                        }
                        if (CommonDataUtil.isNotEmpty(listMailCC)) {
                            for (String toCC : listMailCC) {
                                replyMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(toCC));
                            }
                        }
                        if (CommonDataUtil.isNotEmpty(listMailBCC)) {
                            for (String toBCC : listMailBCC) {
                                replyMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(toBCC));
                            }
                        }
                        // Add content type HTML
                        Multipart multipart = new MimeMultipart();
                        BodyPart messageBodyPart = new MimeBodyPart();
                        messageBodyPart.setContent(content, "text/html");
                        multipart.addBodyPart(messageBodyPart);

                        // Add attachment files
                        if (CommonDataUtil.isNotNull(attachments)) {
                            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                                messageBodyPart = new MimeBodyPart();
                                DataSource source = new FileDataSource(entry.getValue());
                                messageBodyPart.setDataHandler(new DataHandler(source));
                                messageBodyPart.setFileName(entry.getKey());
                                multipart.addBodyPart(messageBodyPart);
                            }
                        }
                        replyMessage.setSubject(subject);
                        replyMessage.setContent(multipart);
                        Transport.send(replyMessage);
                    }
                    // close the store and folder objects
                    folder.close(false);
                    store.close();

            } else {
                for (String toEmail : receivers) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
                }
                if (listMailCC != null) {
                    listMailCC.removeAll(receivers);
                }
                if (CommonDataUtil.isNotEmpty(listMailCC)) {
                    for (String toCC : listMailCC) {
                        message.addRecipient(Message.RecipientType.CC, new InternetAddress(toCC));
                    }
                }
                if (CommonDataUtil.isNotEmpty(listMailBCC)) {
                    for (String toBCC : listMailBCC) {
                        message.addRecipient(Message.RecipientType.BCC, new InternetAddress(toBCC));
                    }
                }

                // Add content type HTML
                Multipart multipart = new MimeMultipart();
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(content, "text/html");
                multipart.addBodyPart(messageBodyPart);

                // Add attachment files
                if (CommonDataUtil.isNotNull(attachments)) {
                    for (Map.Entry<String, String> entry : attachments.entrySet()) {
                        messageBodyPart = new MimeBodyPart();
                        DataSource source = new FileDataSource(entry.getValue());
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(entry.getKey());
                        multipart.addBodyPart(messageBodyPart);
                    }
                }
                message.setSubject(subject);
                message.setContent(multipart);
                Transport.send(message);
                log.info("Sent message successfully");
            }


        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new BusinessException("Fail to send mail.");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }



    public void sendMail(String subject, String content, List<String> receivers,
                         List<String> listMailCC, List<String> listMailBCC, Map<String, String> attachments) {
        ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
        emailExecutor.execute(() -> doSendMail(subject, content, receivers, listMailCC, listMailBCC, attachments));
        emailExecutor.shutdown();
    }

}
