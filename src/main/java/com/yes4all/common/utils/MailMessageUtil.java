package com.yes4all.common.utils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;

public class MailMessageUtil {

    public static Multipart messageMultipart(String content, List<File> listOfFlies) throws MessagingException {

        BodyPart messageBody = new MimeBodyPart();
        messageBody.setText(content);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBody);
        addFileToMultipart(listOfFlies, multipart);

        return multipart;
    }

    private static void addFileToMultipart(List<File> listOfFlies, Multipart multipart) {

        listOfFlies.stream().filter(File::isFile).forEach(f -> {
            try {
                multipart.addBodyPart(addAttachment(f));
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
    }

    private static MimeBodyPart addAttachment(File file) throws MessagingException {

        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(file);
        attachmentBodyPart.setDataHandler(new DataHandler(source));
        attachmentBodyPart.setFileName(file.getName());

        return attachmentBodyPart;
    }

}
