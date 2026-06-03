package com.vocabverse.notification.email;

public interface EmailSender {

    void send(String to, String subject, String body);
}
