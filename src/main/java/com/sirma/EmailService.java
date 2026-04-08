package com.sirma;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailService {

    public static void sendEmail(String tableContent) {
        // 1. Load configuration from config.properties file
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
        } catch (IOException e) {
            System.err.println("Critical Error: Could not load config.properties! Make sure the file exists.");
            return;
        }

        // 2. Extract credentials from the loaded properties
        final String username = config.getProperty("mail.username");
        final String password = config.getProperty("mail.password");

        // 3. SMTP server configuration
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        // 4. Create session with authentication
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));

            // Send report to the same email address loaded from config
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));

            // --- DATE FORMAT dd-MM-yyyy ---
            String dateToday = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            message.setSubject("Insurance Offers Report - " + dateToday);

            // 5. Build HTML content
            String htmlContent = "<h3>Extracted Insurance Offers:</h3>" +
                                 "<p>Date of report: " + dateToday + "</p>" +
                                 "<pre style='font-family: monospace;'>" + tableContent + "</pre>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            // 6. Send the email
            Transport.send(message);
            System.out.println("--- Email sent successfully! ---");

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}