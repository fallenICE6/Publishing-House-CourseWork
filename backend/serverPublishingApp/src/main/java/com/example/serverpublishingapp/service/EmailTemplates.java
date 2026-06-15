package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.entity.Order;
import com.example.serverpublishingapp.entity.OrderFile;
import com.example.serverpublishingapp.entity.OrderMaterial;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class EmailTemplates {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"));

    private EmailTemplates() {}

    public static String buildStatusChangeEmail(Order order, String oldStatus, String newStatus) {
        String fullName = escapeHtml(order.getUser().getFullName());
        String serviceTitle = escapeHtml(order.getService().getTitle());
        String oldStatusEscaped = escapeHtml(oldStatus);
        String newStatusEscaped = escapeHtml(newStatus);
        String createdAt = order.getCreatedAt().format(DATE_FORMATTER);

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
        String createdAt = order.getCreatedAt().format(DATE_FORMATTER);
        String category = order.getService().getCategory();
        boolean isPrinting = "printing".equalsIgnoreCase(category);

        String quantityHtml = "";
        if (isPrinting && order.getQuantity() != null && order.getQuantity() > 0) {
            quantityHtml = String.format("""
            <tr>
                <td style="padding: 8px 0; color: #666;">Тираж:</td>
                <td style="padding: 8px 0; text-align: right;"><strong>%d шт</strong></td>
            </tr>
            """, order.getQuantity());
        }

        String pagesHtml = "";
        if (order.getPages() != null && order.getPages() > 0) {
            pagesHtml = String.format("""
            <tr>
                <td style="padding: 8px 0; color: #666;">Количество страниц:</td>
                <td style="padding: 8px 0; text-align: right;"><strong>%d</strong></td>
            </tr>
            """, order.getPages());
        }

        // Материалы
        StringBuilder materialsHtml = new StringBuilder();
        if (order.getMaterials() != null && !order.getMaterials().isEmpty()) {
            materialsHtml.append("""
            <div style="margin-top: 20px;">
                <h3 style="color: #6B5B95; margin-bottom: 12px;">📦 Выбранные материалы</h3>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr>
                            <th style="padding: 8px 0; text-align: left; border-bottom: 2px solid #6B5B95; color: #6B5B95;">Материал</th>
                            <th style="padding: 8px 0; text-align: center; border-bottom: 2px solid #6B5B95; color: #6B5B95;">Кол-во</th>
                            <th style="padding: 8px 0; text-align: right; border-bottom: 2px solid #6B5B95; color: #6B5B95;">Цена</th>
                            <th style="padding: 8px 0; text-align: right; border-bottom: 2px solid #6B5B95; color: #6B5B95;">Стоимость</th>
                        <tr>
                    </thead>
                    <tbody>
        """);

            for (OrderMaterial material : order.getMaterials()) {
                String categoryRu = switch (material.getMaterial().getCategory()) {
                    case paper -> "Бумага";
                    case cover -> "Обложка";
                    case binding -> "Переплёт";
                };
                BigDecimal totalPrice = material.getPrice().multiply(BigDecimal.valueOf(material.getQuantity()));
                materialsHtml.append(String.format("""
                         <tr>
                            <td style="padding: 8px 0; border-bottom: 1px solid #eee;">%s: %s</td>
                            <td style="padding: 8px 0; text-align: center; border-bottom: 1px solid #eee;">%d</td>
                            <td style="padding: 8px 0; text-align: right; border-bottom: 1px solid #eee;">%.2f ₽</td>
                            <td style="padding: 8px 0; text-align: right; border-bottom: 1px solid #eee;">%.2f ₽</td>
                         </tr>
                        """,
                        categoryRu,
                        escapeHtml(material.getMaterial().getName()),
                        material.getQuantity(),
                        material.getPrice(),
                        totalPrice
                ));
            }

            materialsHtml.append("""
                    </tbody>
                </table>
            </div>
        """);
        }

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
                    background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%);
                    color: white;
                    padding: 30px 20px;
                    text-align: center;
                    border-radius: 12px 12px 0 0;
                }
                .header h1 {
                    margin: 0;
                    font-size: 28px;
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
                    border-left: 4px solid #4CAF50;
                    padding: 20px;
                    margin: 20px 0;
                    border-radius: 8px;
                }
                .order-info table {
                    width: 100%%;
                }
                .total-price {
                    background: #4CAF50;
                    color: white;
                    padding: 15px 20px;
                    border-radius: 8px;
                    text-align: center;
                    margin-top: 20px;
                }
                .total-price span {
                    font-size: 28px;
                    font-weight: bold;
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
                    background: #4CAF50;
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
                    <h1>✅ Заказ подтверждён!</h1>
                    <p>BookHouse — профессиональное издательство</p>
                </div>
                <div class="content">
                    <h2>Здравствуйте, %s!</h2>
                    <p>Ваш заказ <strong>№%d</strong> успешно создан и передан в обработку.</p>
                    
                    <div class="order-info">
                        <h3 style="margin:0 0 15px 0; color:#6B5B95;">📋 Детали заказа</h3>
                        <table style="width: 100%%;">
                            <tr>
                                <td style="padding: 8px 0; color: #666;">Услуга:</td>
                                <td style="padding: 8px 0; text-align: right;"><strong>%s</strong></td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #666;">Дата создания:</td>
                                <td style="padding: 8px 0; text-align: right;"><strong>%s</strong></td>
                            </tr>
                            %s
                            %s
                        </table>
                        %s
                        <div class="total-price">
                            Общая стоимость: <span>%.2f ₽</span>
                        </div>
                    </div>
                    
                    <div style="text-align: center; margin: 20px 0;">
                        <a href="#" class="button">📱 Перейти в личный кабинет</a>
                    </div>
                    
                    <p style="text-align: center; color: #666;">Наши специалисты свяжутся с вами в ближайшее время<br>для уточнения деталей заказа.</p>
                    
                    <div class="footer">
                        <p>Это письмо отправлено автоматически. Пожалуйста, не отвечайте на него.</p>
                        <p>© 2026 BookHouse | Все права защищены</p>
                        <p>📞 +7 (930) 276-25-81 | ✉️ support@bookhouse.ru</p>
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
                materialsHtml.toString(),
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