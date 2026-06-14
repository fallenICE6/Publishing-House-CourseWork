package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.Order;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class EmailTemplates {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"));

    private EmailTemplates() {}

    public static String buildStatusChangeEmail(Order order, String oldStatus, String newStatus) {
        // Экранируем все строковые значения
        String fullName = escapeHtml(order.getUser().getFullName());
        String serviceTitle = escapeHtml(order.getService().getTitle());
        String oldStatusEscaped = escapeHtml(oldStatus);
        String newStatusEscaped = escapeHtml(newStatus);
        String createdAt = order.getCreatedAt().format(DATE_FORMATTER);

        // Формируем строки для тиража и страниц
        String quantityHtml = "";
        if (order.getQuantity() != null && order.getQuantity() > 0) {
            quantityHtml = String.format("</tr><tr><td>Тираж:</td><td><strong>%d шт</strong></td>", order.getQuantity());
        }

        String pagesHtml = "";
        if (order.getPages() != null && order.getPages() > 0) {
            pagesHtml = String.format("</tr><tr><td>Количество страниц:</td><td><strong>%d</strong></td>", order.getPages());
        }

        String statusClass = getStatusClass(newStatusEscaped);
        String actionButton = getActionButton(newStatusEscaped);
        String statusMessage = getStatusMessage(newStatusEscaped);

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Segoe UI', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #6B5B95 0%%, #8B7BB5 100%%);
                        color: white;
                        padding: 30px 20px;
                        text-align: center;
                        border-radius: 12px 12px 0 0;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .header p {
                        margin: 8px 0 0;
                        opacity: 0.9;
                    }
                    .content {
                        background: white;
                        padding: 30px;
                        border-radius: 0 0 12px 12px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .order-info {
                        background: #f8f6ff;
                        border-left: 4px solid #6B5B95;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 8px;
                    }
                    .status-box {
                        background: #fff8e1;
                        border-radius: 12px;
                        padding: 20px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .status-old {
                        color: #999;
                        text-decoration: line-through;
                        font-size: 18px;
                    }
                    .status-arrow {
                        font-size: 28px;
                        margin: 10px 0;
                        color: #6B5B95;
                    }
                    .status-new {
                        color: #4CAF50;
                        font-size: 24px;
                        font-weight: bold;
                    }
                    .status-new.canceled { color: #f44336; }
                    .status-new.completed { color: #2196F3; }
                    .details-table {
                        width: 100%%;
                        border-collapse: collapse;
                        margin: 15px 0;
                    }
                    .details-table td {
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .details-table td:first-child {
                        font-weight: bold;
                        width: 40%%;
                        color: #6B5B95;
                    }
                    .message-box {
                        background: #e8f5e9;
                        padding: 15px;
                        border-radius: 8px;
                        margin: 20px 0;
                    }
                    .footer {
                        text-align: center;
                        padding: 20px;
                        color: #999;
                        font-size: 12px;
                        border-top: 1px solid #eee;
                        margin-top: 20px;
                    }
                    .button {
                        display: inline-block;
                        background: #6B5B95;
                        color: white;
                        text-decoration: none;
                        padding: 12px 24px;
                        border-radius: 25px;
                        margin: 15px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📬 BookHouse</h1>
                        <p>Типография профессиональной печати</p>
                    </div>
                    <div class="content">
                        <h2>Здравствуйте, %s!</h2>
                        <p>Статус вашего заказа <strong>№%d</strong> изменился.</p>
                        
                        <div class="order-info">
                            <h3 style="margin:0 0 10px 0;">📋 Детали заказа</h3>
                            <table class="details-table">
                                <tr><td>Услуга:</td><td><strong>%s</strong></td></tr>
                                <tr><td>Дата создания:</td><td><strong>%s</strong></td></tr>
                                %s
                                %s
                                <tr><td>Стоимость:</td><td><strong style="color:#6B5B95; font-size:18px;">%.2f ₽</strong></td></tr>
                            </table>
                        </div>
                        
                        <div class="status-box">
                            <div class="status-old">%s</div>
                            <div class="status-arrow">▼</div>
                            <div class="status-new %s">%s</div>
                        </div>
                        
                        %s
                        
                        <div class="message-box">
                            %s
                        </div>
                        
                        <div class="footer">
                            <p>Это письмо отправлено автоматически. Пожалуйста, не отвечайте на него.</p>
                            <p>© 2024 BookHouse | Все права защищены</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
                fullName,
                order.getId(),
                serviceTitle,
                createdAt,
                quantityHtml,
                pagesHtml,
                order.getTotalPrice(),
                oldStatusEscaped,
                statusClass,
                newStatusEscaped,
                actionButton,
                statusMessage
        );
    }

    public static String buildOrderCreatedEmail(Order order) {
        String fullName = escapeHtml(order.getUser().getFullName());
        String serviceTitle = escapeHtml(order.getService().getTitle());

        String quantityHtml = "";
        if (order.getQuantity() != null && order.getQuantity() > 0) {
            quantityHtml = String.format("<p><strong>Тираж:</strong> %d шт</p>", order.getQuantity());
        }

        String pagesHtml = "";
        if (order.getPages() != null && order.getPages() > 0) {
            pagesHtml = String.format("<p><strong>Страниц:</strong> %d</p>", order.getPages());
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f5f5f5; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 30px 20px; text-align: center; border-radius: 12px 12px 0 0; }
                    .content { background: white; padding: 30px; border-radius: 0 0 12px 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .success-icon { font-size: 48px; text-align: center; margin-bottom: 20px; }
                    .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Заказ подтверждён!</h1>
                    </div>
                    <div class="content">
                        <div class="success-icon">🎉</div>
                        <h2>Здравствуйте, %s!</h2>
                        <p>Ваш заказ <strong>№%d</strong> успешно создан и передан в обработку.</p>
                        <div style="background: #f8f6ff; border-left: 4px solid #4CAF50; padding: 15px; margin: 20px 0; border-radius: 8px;">
                            <h3 style="margin:0 0 10px 0;">📋 Информация о заказе</h3>
                            <p><strong>Услуга:</strong> %s</p>
                            %s
                            %s
                            <p><strong>Общая стоимость:</strong> <span style="color:#4CAF50; font-size:20px;">%.2f ₽</span></p>
                        </div>
                        <p>Мы свяжемся с вами для уточнения деталей в ближайшее время.</p>
                        <div class="footer">
                            <p>© 2024 BookHouse | Все права защищены</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
                fullName,
                order.getId(),
                serviceTitle,
                quantityHtml,
                pagesHtml,
                order.getTotalPrice()
        );
    }

    private static String getStatusClass(String status) {
        if (status == null) return "";
        return switch (status) {
            case "Отменён" -> "canceled";
            case "Завершён" -> "completed";
            default -> "";
        };
    }

    private static String getActionButton(String status) {
        if ("Готов к печати".equals(status)) {
            return """
                <div style="text-align: center;">
                    <a href="#" class="button">📄 Скачать макет</a>
                </div>
                """;
        }
        return "";
    }

    private static String getStatusMessage(String status) {
        if (status == null) return "";

        return switch (status) {
            case "Создан" ->
                    "Ваш заказ создан и ожидает обработки.";
            case "На проверке" ->
                    "🔄 <strong>Ваш заказ передан на проверку</strong><br>" +
                            "Наши специалисты проверят макет и свяжутся с вами в ближайшее время.";
            case "Редактируется" ->
                    "✏️ <strong>Требуется доработка</strong><br>" +
                            "Пожалуйста, проверьте комментарии к заказу и внесите необходимые правки.";
            case "Готов к печати" ->
                    "🖨️ <strong>Заказ готов к печати!</strong><br>" +
                            "Макет одобрен, заказ передан в производство.";
            case "Завершён" ->
                    "✅ <strong>Заказ выполнен!</strong><br>" +
                            "Спасибо, что выбрали нас! Ждём вас снова.";
            case "Отменён" ->
                    "❌ <strong>Заказ отменён</strong><br>" +
                            "Если это произошло по ошибке, пожалуйста, свяжитесь с нами.";
            default -> "Статус заказа обновлён.";
        };
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}