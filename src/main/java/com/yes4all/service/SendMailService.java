package com.yes4all.service;

import java.util.List;
import java.util.Map;

public interface SendMailService {
    void sendMail(String subject, String content, List<String> receivers,
                  List<String> listMailCC, List<String> listMailBCC, Map<String, String> attachments);

}
