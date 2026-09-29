package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("oplan00000@gmail.com");  // 差出人を明示する
            
            // メール送信前にログを追加
            System.out.println("❗ メール送信開始: " + to + ", " + subject);
            System.out.println("📬 差出人: " + message.getFrom());
            System.out.println("📬 宛先: " + String.join(", ", message.getTo()));
            System.out.println("📬 件名: " + message.getSubject());
            System.out.println("📬 本文: " + message.getText());

            
            mailSender.send(message);
            
            System.out.println("✅ メール送信成功！");
        } catch (Exception e) {
            System.out.println("❌ メール送信エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
