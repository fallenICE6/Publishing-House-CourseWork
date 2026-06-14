package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company-name:BookHouse}")
    private String companyName;

    @Value("${app.support-email:support@bookhouse.ru}")
    private String supportEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendOrderStatusChangedEmail(Order order, String oldStatus, String newStatus) {
        if (!shouldSendEmail(order)) {
            logger.debug("Email not sent for order {}: user has no email", order.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, companyName);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(generateSubject(order, newStatus));
            helper.setText(EmailTemplates.buildStatusChangeEmail(order, oldStatus, newStatus), true);

            mailSender.send(message);
            logger.info("Status change email sent for order {}: {} -> {}",
                    order.getId(), oldStatus, newStatus);

        } catch (MessagingException e) {
            logger.error("Failed to send status change email for order {}: {}",
                    order.getId(), e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendOrderCreatedEmail(Order order, String customerEmail) {
        if (customerEmail == null || customerEmail.isEmpty()) {
            logger.debug("Order created email not sent for order {}: no email provided", order.getId());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, companyName);
            helper.setTo(customerEmail);
            helper.setSubject(String.format("✅ Заказ №%d подтверждён - BookHouse", order.getId()));
            helper.setText(EmailTemplates.buildOrderCreatedEmail(order), true);

            mailSender.send(message);
            logger.info("Order created email sent for order {}", order.getId());

        } catch (MessagingException e) {
            logger.error("Failed to send order created email for order {}: {}",
                    order.getId(), e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldSendEmail(Order order) {
        return order.getUser() != null
                && order.getUser().getEmail() != null
                && !order.getUser().getEmail().isEmpty();
    }

    private String generateSubject(Order order, String newStatus) {
        String emoji = switch (newStatus) {
            case "Завершён" -> "✅";
            case "Отменён" -> "❌";
            case "Готов к печати" -> "🖨️";
            case "На проверке" -> "🔍";
            case "Редактируется" -> "✏️";
            default -> "📬";
        };
        return String.format("%s Заказ №%d - %s", emoji, order.getId(), newStatus);
    }
}