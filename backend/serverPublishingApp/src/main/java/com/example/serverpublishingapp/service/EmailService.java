package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.Order;
import com.example.serverpublishingapp.entity.User;
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

    @Async
    public void sendCommentToAuthor(Order order, User editor, String comment) {
        if (!shouldSendEmail(order)) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, companyName);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(String.format("💬 Новый комментарий к заказу №%d", order.getId()));
            helper.setText(String.format("""
            <h3>Новый комментарий от редактора</h3>
            <p><strong>Заказ №%d:</strong> %s</p>
            <div style="background: #f3e5f5; padding: 15px; border-radius: 8px; border-left: 4px solid #9c27b0;">
                <p><strong>%s:</strong></p>
                <p>%s</p>
            </div>
            <p>Вы можете ответить на комментарий и перезагрузить файлы в личном кабинете.</p>
            <hr>
            <p style="color: #666; font-size: 12px;">© BookHouse</p>
            """,
                    order.getId(),
                    order.getService().getTitle(),
                    editor.getFullName(),
                    comment
            ), true);
            mailSender.send(message);
            logger.info("Comment notification sent to author for order {}", order.getId());
        } catch (Exception e) {
            logger.error("Failed to send comment to author: {}", e.getMessage());
        }
    }

    @Async
    public void sendCommentToEditor(Order order, User author, String comment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, companyName);
            helper.setTo(supportEmail);
            helper.setSubject(String.format("💬 Автор ответил на комментарий к заказу №%d", order.getId()));
            helper.setText(String.format("""
            <h3>Автор %s ответил на комментарий</h3>
            <p><strong>Заказ №%d:</strong> %s</p>
            <div style="background: #e8f5e9; padding: 15px; border-radius: 8px; border-left: 4px solid #4caf50;">
                <p><strong>%s:</strong></p>
                <p>%s</p>
            </div>
            <p>Перейдите в панель редактора для продолжения работы.</p>
            """,
                    author.getFullName(),
                    order.getId(),
                    order.getService().getTitle(),
                    author.getFullName(),
                    comment
            ), true);
            mailSender.send(message);
            logger.info("Comment notification sent to editor for order {}", order.getId());
        } catch (Exception e) {
            logger.error("Failed to send comment to editor: {}", e.getMessage());
        }
    }

    @Async
    public void sendFilesReuploadedToEditor(Order order, User author, String comment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, companyName);
            helper.setTo(supportEmail);
            helper.setSubject(String.format("📎 Автор перезагрузил файлы для заказа №%d", order.getId()));
            helper.setText(String.format("""
            <h3>Автор %s перезагрузил файлы</h3>
            <p><strong>Заказ №%d:</strong> %s</p>
            <p>Заказ остаётся в статусе <strong>«Редактируется»</strong></p>
            %s
            <p>Пожалуйста, проверьте новые файлы и нажмите <strong>«Отправить на проверку»</strong> когда будете готовы.</p>
            """,
                    author.getFullName(),
                    order.getId(),
                    order.getService().getTitle(),
                    (comment != null && !comment.isBlank())
                            ? "<p><strong>Комментарий автора:</strong> " + comment + "</p>"
                            : ""
            ), true);
            mailSender.send(message);
            logger.info("Files reuploaded notification sent to editor for order {}", order.getId());
        } catch (Exception e) {
            logger.error("Failed to send reupload notification to editor: {}", e.getMessage());
        }
    }

    @Async
    public void sendOrderReadyForReviewToReviewer(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, companyName);
            helper.setTo(supportEmail);
            helper.setSubject(String.format("🔄 Заказ №%d готов к повторной проверке", order.getId()));
            helper.setText(String.format("""
            <h3>Заказ №%d готов к проверке</h3>
            <p><strong>Автор:</strong> %s</p>
            <p><strong>Услуга:</strong> %s</p>
            <p>Заказ переведён в статус <strong>«На проверке»</strong></p>
            <p>Пожалуйста, проверьте заказ в панели рецензента.</p>
            """,
                    order.getId(),
                    order.getUser().getFullName(),
                    order.getService().getTitle()
            ), true);
            mailSender.send(message);
            logger.info("Ready for review notification sent for order {}", order.getId());
        } catch (Exception e) {
            logger.error("Failed to send ready for review notification: {}", e.getMessage());
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