package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.config.OrderMapper;
import com.example.serverpublishingapp.dto.*;
import com.example.serverpublishingapp.entity.*;
import com.example.serverpublishingapp.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderMaterialRepository orderMaterialRepo;
    private final OrderFileRepository orderFileRepo;
    private final MaterialRepository materialRepo;
    private final PublishingServiceRepository serviceRepo;
    private final OrderMapper mapper;

    private final JavaMailSender mailSender;

    @Autowired
    public OrderService(OrderRepository orderRepo,
                        OrderMaterialRepository orderMaterialRepo,
                        OrderFileRepository orderFileRepo,
                        MaterialRepository materialRepo,
                        PublishingServiceRepository serviceRepo,
                        OrderMapper mapper,
                        JavaMailSender mailSender) {
        this.orderRepo = orderRepo;
        this.orderMaterialRepo = orderMaterialRepo;
        this.orderFileRepo = orderFileRepo;
        this.materialRepo = materialRepo;
        this.serviceRepo = serviceRepo;
        this.mapper = mapper;
        this.mailSender = mailSender;
    }

    public OrderDto create(CreateOrderRequest req, List<MultipartFile> files, User user) {

        PublishingService service = serviceRepo.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        int pages = req.getPages() != null ? req.getPages() : 0;
        int quantity = req.getQuantity() != null ? req.getQuantity() : 1;

        Order order = new Order();
        order.setUser(user);
        order.setService(service);
        order.setPages(pages);
        order.setQuantity(quantity);
        order = orderRepo.save(order);

        BigDecimal total = service.getPrice();

        if (req.getMaterials() != null) {
            for (OrderMaterialRequest m : req.getMaterials()) {

                Material material = materialRepo.findById(m.getMaterialId())
                        .orElseThrow(() -> new RuntimeException("Material not found"));

                int materialQuantity;
                BigDecimal materialCost;

                switch (material.getCategory()) {
                    case paper -> {
                        materialQuantity = pages * quantity; // paper = страницы * тираж
                        materialCost = material.getPrice().multiply(BigDecimal.valueOf(materialQuantity));
                    }
                    case cover, binding -> {
                        materialQuantity = quantity; // cover и binding = тираж
                        materialCost = material.getPrice().multiply(BigDecimal.valueOf(materialQuantity));
                    }
                    default -> throw new RuntimeException("Unknown material category");
                }

                OrderMaterial om = new OrderMaterial();
                om.setOrder(order);
                om.setMaterial(material);
                om.setQuantity(materialQuantity);
                om.setPrice(material.getPrice());
                orderMaterialRepo.save(om);

                total = total.add(materialCost);
            }
        }

        if (files != null && !files.isEmpty()) {
            Path dir = Paths.get("uploads/orders/" + order.getId());
            try { Files.createDirectories(dir); } catch (IOException e) { throw new RuntimeException(e); }

            for (MultipartFile file : files) {
                String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
                String filename = UUID.randomUUID() + "_" + originalName;
                Path path = dir.resolve(filename);

                try { Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); }
                catch (IOException e) { throw new RuntimeException(e); }

                OrderFile of = new OrderFile();
                of.setOrder(order);
                of.setFileName(originalName);
                of.setFileType(file.getContentType());
                of.setFilePath(path.toString());
                orderFileRepo.save(of);
            }
        }

        order.setTotalPrice(total.setScale(2, RoundingMode.HALF_UP));
        order = orderRepo.save(order);

        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            sendOrderEmail(req.getEmail(), order);
        }

        return mapper.toDto(order);
    }

    private void sendOrderEmail(String toEmail, Order order) {
        StringBuilder body = new StringBuilder();
        body.append("Здравствуйте, ").append(order.getUser().getFullName()).append("!\n\n")
                .append("Ваш заказ №").append(order.getId()).append(" успешно создан.\n\n")
                .append("Информация о заказе:\n")
                .append("Услуга: ").append(order.getService().getTitle()).append("\n");

        if (order.getQuantity() != null && order.getQuantity() > 0) {
            body.append("Тираж: ").append(order.getQuantity()).append("\n");
        }

        if (order.getPages() != null && order.getPages() > 0) {
            body.append("Листы: ").append(order.getPages()).append("\n");
        }

        body.append("Общая стоимость: ").append(order.getTotalPrice()).append(" ₽\n\n")
                .append("Мы свяжемся с вами для уточнения деталей заказа.\n\n")
                .append("Спасибо, что выбрали нашу типографию!");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("bookhouse456@mail.ru");
        message.setTo(toEmail);
        message.setSubject("Ваш заказ №" + order.getId() + " принят");
        message.setText(body.toString());

        mailSender.send(message);
    }

    public List<OrderFullDto> getMyOrders(User user) {

        return orderRepo.findByUserId(user.getId()).stream()
                .map(order -> {

                    List<OrderMaterialDto> materials = order.getMaterials().stream()
                            .map(m -> new OrderMaterialDto(
                                    m.getMaterial().getName(),
                                    m.getMaterial().getCategory().name(),
                                    m.getQuantity(),
                                    m.getPrice()
                            ))
                            .toList();

                    List<OrderFileDto> files = order.getFiles().stream()
                            .map(f -> new OrderFileDto(
                                    f.getId(),
                                    f.getFileName(),
                                    f.getFileType(),
                                    "/api/files/" + f.getId() + "/download"
                            ))
                            .toList();

                    Review review = order.getReview();
                    ReviewDto reviewDto = null;

                    if (review != null) {
                        reviewDto = new ReviewDto(
                                review.getComment(),
                                translateReviewStatus(review.getStatus())
                        );
                    }

                    return new OrderFullDto(
                            order.getId(),
                            order.getUser().getFullName(),
                            order.getUser().getEmail(),
                            order.getUser().getPhone(),
                            order.getService().getTitle(),
                            order.getPages(),
                            order.getQuantity(),
                            materials,
                            files,
                            reviewDto,
                            order.getTotalPrice(),
                            translateOrderStatus(order.getStatus().toString()),
                            order.getCreatedAt()
                    );
                })
                .toList();
    }
    private String translateOrderStatus(String status) {
        return switch (status.toLowerCase()) {
            case "created" -> "Создан";
            case "under_review" -> "На проверке";
            case "editing" -> "Редактируется";
            case "ready_for_print" -> "Готов к печати";
            case "completed" -> "Завершён";
            case "canceled" -> "Отменён";
            default -> "Неизвестно";
        };
    }

    private String translateReviewStatus(String status) {
        return switch (status) {
            case "pending" -> "На проверке";
            case "approved" -> "Одобрена";
            case "rejected" -> "Отклонена";
            default -> "—";
        };
    }

    public OrderFullDto getOrderById(Long orderId, User user) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        List<OrderMaterialDto> materials = order.getMaterials().stream()
                .map(m -> new OrderMaterialDto(
                        m.getMaterial().getName(),
                        m.getMaterial().getCategory().name(),
                        m.getQuantity(),
                        m.getPrice()
                ))
                .toList();

        List<OrderFileDto> files = order.getFiles().stream()
                .map(f -> new OrderFileDto(
                        f.getId(),
                        f.getFileName(),
                        f.getFileType(),
                        "/api/files/" + f.getId() + "/download"
                ))
                .toList();

        Review review = order.getReview();
        ReviewDto reviewDto = null;

        if (review != null) {
            reviewDto = new ReviewDto(
                    review.getComment(),
                    translateReviewStatus(review.getStatus())
            );
        }

        return new OrderFullDto(
                order.getId(),
                order.getUser().getFullName(),
                order.getUser().getEmail(),
                order.getUser().getPhone(),
                order.getService().getTitle(),
                order.getPages(),
                order.getQuantity(),
                materials,
                files,
                reviewDto,
                order.getTotalPrice(),
                translateOrderStatus(order.getStatus().toString()),
                order.getCreatedAt()
        );
    }

    public List<OrderFullDto> getAllOrders(String search, String status) {
        List<Order> orders;

        if (search != null && !search.isEmpty()) {
            // Убираем пробелы по краям
            String trimmedSearch = search.trim();

            // Пытаемся найти по ID или ФИО
            orders = orderRepo.searchByOrderIdOrFullName(trimmedSearch);

            // Если ничего не найдено, возвращаем пустой список
            if (orders.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            orders = orderRepo.findAll();
        }

        // Фильтрация по статусу
        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
            orders = orders.stream()
                    .filter(order -> order.getStatus().toString().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        // Сортировка по дате (новые сверху)
        orders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

        return orders.stream()
                .map(this::mapToOrderFullDto)
                .collect(Collectors.toList());
    }

    public OrderFullDto updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, User admin) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Order.Status currentStatus = order.getStatus();
        Order.Status newStatus = request.getStatusAsEnum();

        // Валидация переходов статусов
        validateStatusTransition(currentStatus, newStatus);

        // Обновляем статус
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepo.save(order);

        return mapToOrderFullDto(order);
    }

    private void validateStatusTransition(Order.Status currentStatus, Order.Status newStatus) {
        // Определяем разрешенные переходы
        switch (currentStatus) {
            case created:
                if (newStatus != Order.Status.under_review) {
                    throw new RuntimeException("Из статуса 'Создан' можно перейти только в 'На проверке'");
                }
                break;

            case under_review:
                if (!(newStatus == Order.Status.editing ||
                        newStatus == Order.Status.ready_for_print ||
                        newStatus == Order.Status.canceled)) {
                    throw new RuntimeException("Из статуса 'На проверке' можно перейти только в 'Редактируется', 'Готов к печати' или 'Отменён'");
                }
                break;

            case editing:
                if (newStatus != Order.Status.under_review) {
                    throw new RuntimeException("Из статуса 'Редактируется' можно вернуться только в 'На проверке'");
                }
                break;

            case ready_for_print:
                if (newStatus != Order.Status.completed) {
                    throw new RuntimeException("Из статуса 'Готов к печати' можно перейти только в 'Завершён'");
                }
                break;

            case completed:
            case canceled:
                throw new RuntimeException("Завершённые и отменённые заказы нельзя изменять");

            default:
                throw new RuntimeException("Неизвестный статус");
        }
    }

    public OrderFullDto getOrderByIdAdmin(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToOrderFullDto(order);
    }

    private OrderFullDto mapToOrderFullDto(Order order) {
        List<OrderMaterialDto> materials = order.getMaterials().stream()
                .map(m -> new OrderMaterialDto(
                        m.getMaterial().getName(),
                        m.getMaterial().getCategory().name(),
                        m.getQuantity(),
                        m.getPrice()
                ))
                .collect(Collectors.toList());

        List<OrderFileDto> files = order.getFiles().stream()
                .map(f -> new OrderFileDto(
                        f.getId(),
                        f.getFileName(),
                        f.getFileType(),
                        "/api/files/" + f.getId() + "/download"
                ))
                .collect(Collectors.toList());

        Review review = order.getReview();
        ReviewDto reviewDto = null;

        if (review != null) {
            reviewDto = new ReviewDto(
                    review.getComment(),
                    translateReviewStatus(review.getStatus())
            );
        }

        return new OrderFullDto(
                order.getId(),
                order.getUser().getFullName(),
                order.getUser().getEmail(),
                order.getUser().getPhone(),
                order.getService().getTitle(),
                order.getPages(),
                order.getQuantity(),
                materials,
                files,
                reviewDto,
                order.getTotalPrice(),
                translateOrderStatus(order.getStatus().toString()),
                order.getCreatedAt()
        );
    }


}
