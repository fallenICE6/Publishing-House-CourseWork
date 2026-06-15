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
    private final ReviewRepository reviewRepo;
    private final OrderMapper mapper;

    private final JavaMailSender mailSender;
    private final EmailService emailService;

    private final OrderCommentRepository orderCommentRepository;

    @Autowired
    public OrderService(OrderRepository orderRepo,
                        OrderMaterialRepository orderMaterialRepo,
                        OrderFileRepository orderFileRepo,
                        MaterialRepository materialRepo,
                        PublishingServiceRepository serviceRepo,
                        ReviewRepository reviewRepo,
                        OrderMapper mapper,
                        JavaMailSender mailSender,
                        EmailService emailService,
                        OrderCommentRepository orderCommentRepository) {
        this.orderRepo = orderRepo;
        this.orderMaterialRepo = orderMaterialRepo;
        this.orderFileRepo = orderFileRepo;
        this.materialRepo = materialRepo;
        this.serviceRepo = serviceRepo;
        this.reviewRepo = reviewRepo;
        this.mapper = mapper;
        this.mailSender = mailSender;
        this.emailService = emailService;
        this.orderCommentRepository = orderCommentRepository;
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
            Order completeOrder = orderRepo.findById(order.getId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            emailService.sendOrderCreatedEmail(completeOrder, req.getEmail());
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
                .map(this::mapToOrderFullDto)
                .collect(Collectors.toList());
    }

    public String translateOrderStatus(String status) {
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

    public String translateReviewStatus(String status) {
        return switch (status) {
            case "pending" -> "На доработку";
            case "approved" -> "Одобрено";
            case "rejected" -> "Отклонено";
            default -> "—";
        };
    }
    public OrderFullDto getOrderByIdForEditor(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.Status.editing) {
            throw new RuntimeException("Редактор может просматривать только заказы в статусе 'Редактируется'");
        }

        return mapToOrderFullDtoWithComments(order);
    }

    public OrderFullDto getOrderById(Long orderId, User user) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToOrderFullDto(order);
    }

    public List<OrderFullDto> getAllOrders(String search, String status) {
        List<Order> orders;

        if (search != null && !search.isEmpty()) {
            String trimmedSearch = search.trim();
            orders = orderRepo.searchByOrderIdOrFullName(trimmedSearch);

            if (orders.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            orders = orderRepo.findAll();
        }

        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
            orders = orders.stream()
                    .filter(order -> order.getStatus().toString().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

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

        validateStatusTransition(currentStatus, newStatus);

        String oldStatusRu = translateOrderStatus(currentStatus.name());
        String newStatusRu = translateOrderStatus(newStatus.name());

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepo.save(order);

        emailService.sendOrderStatusChangedEmail(order, oldStatusRu, newStatusRu);

        return mapToOrderFullDto(order);
    }

    private void validateStatusTransition(Order.Status currentStatus, Order.Status newStatus) {
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


    public List<Order> getOrdersForReview() {
        return orderRepo.findByStatus(Order.Status.under_review);
    }

    @Transactional
    public void updateOrderStatusAfterReview(Long orderId, String newStatus) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Order.Status orderStatus = mapReviewStatusToOrderStatus(newStatus);
        order.setStatus(orderStatus);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepo.save(order);
    }

    private Order.Status mapReviewStatusToOrderStatus(String reviewStatus) {
        switch (reviewStatus) {
            case "editing":
                return Order.Status.editing;
            case "ready_for_print":
                return Order.Status.ready_for_print;
            case "canceled":
                return Order.Status.canceled;
            default:
                throw new RuntimeException("Invalid review status: " + reviewStatus);
        }
    }

    public List<OrderFullDto> getEditingOrdersForEditor() {
        List<Order> orders = orderRepo.findByStatus(Order.Status.editing);
        orders.sort((o1, o2) -> o2.getUpdatedAt().compareTo(o1.getUpdatedAt()));
        return orders.stream().map(this::mapToOrderFullDtoWithComments).collect(Collectors.toList());
    }

    @Transactional
    public OrderCommentDto addCommentToEditingOrder(Long orderId, User user, String commentText) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Разрешаем комментарии только в статусе editing
        if (order.getStatus() != Order.Status.editing) {
            throw new RuntimeException("Комментарии доступны только в статусе 'Редактируется'");
        }

        // Проверка прав
        if (user.getRole() == Role.AUTHOR && !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Вы можете комментировать только свои заказы");
        }
        if (user.getRole() == Role.EDITOR && order.getStatus() != Order.Status.editing) {
            throw new RuntimeException("Редактор может комментировать только заказы в статусе 'Редактируется'");
        }

        OrderComment comment = new OrderComment(order, user, commentText, false);
        OrderComment saved = orderCommentRepository.save(comment);

        // Отправляем email уведомление
        if (user.getRole() == Role.EDITOR) {
            emailService.sendCommentToAuthor(order, user, commentText);
        } else if (user.getRole() == Role.AUTHOR) {
            emailService.sendCommentToEditor(order, user, commentText);
        }

        return new OrderCommentDto(saved.getId(), saved.getUser().getId(),
                saved.getUser().getFullName(), saved.getUser().getRole().name(),
                saved.getComment(), saved.getIsSystem(), saved.getCreatedAt());
    }
    public List<OrderCommentDto> getCommentsByOrderId(Long orderId) {
        return orderCommentRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(c -> new OrderCommentDto(c.getId(), c.getUser().getId(),
                        c.getUser().getFullName(), c.getUser().getRole().name(),
                        c.getComment(), c.getIsSystem(), c.getCreatedAt()))
                .collect(Collectors.toList());
    }
    @Transactional
    public OrderFullDto reuploadFiles(Long orderId, User author, List<MultipartFile> newFiles, String userComment) {
        Order order = validateOrderForAuthor(orderId, author);

        for (OrderFile oldFile : order.getFiles()) {
            try {
                Path path = Paths.get(oldFile.getFilePath());
                Files.deleteIfExists(path);
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + oldFile.getFilePath());
            }
        }
        orderFileRepo.deleteAll(order.getFiles());
        order.getFiles().clear();

        Path dir = Paths.get("uploads/orders/" + order.getId());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (MultipartFile file : newFiles) {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) {
                originalName = "file_" + System.currentTimeMillis();
            }

            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalName.substring(dotIndex);
            }
            String storedFilename = UUID.randomUUID() + extension;
            Path path = dir.resolve(storedFilename);

            try {
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            OrderFile of = new OrderFile();
            of.setOrder(order);
            of.setFileName(originalName);
            of.setFileType(file.getContentType());
            of.setFilePath(path.toString());
            orderFileRepo.save(of);
            order.getFiles().add(of);
        }

        String systemMessage = (userComment != null && !userComment.isBlank())
                ? "🔄 Автор заменил все файлы. Комментарий: " + userComment
                : "🔄 Автор заменил все файлы";

        addSystemComment(order, author, systemMessage);
        emailService.sendFilesReuploadedToEditor(order, author, userComment);

        return mapToOrderFullDtoWithComments(order);
    }

    @Transactional
    public OrderFullDto sendToReview(Long orderId, User editor) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (editor.getRole() != Role.EDITOR) {
            throw new RuntimeException("Только редактор может отправить заказ на проверку");
        }
        if (order.getStatus() != Order.Status.editing) {
            throw new RuntimeException("Отправить на проверку можно только из статуса 'Редактируется'");
        }

        String oldStatusRu = translateOrderStatus(order.getStatus().name());

        if (order.getReview() != null) {
            reviewRepo.delete(order.getReview());
            order.setReview(null);
        }

        order.setStatus(Order.Status.under_review);
        order.setUpdatedAt(LocalDateTime.now());
        order.setReadyForReview(true);
        Order savedOrder = orderRepo.save(order);

        OrderComment systemComment = new OrderComment(order, editor,
                "✅ Редактор отправил заказ на проверку", true);
        orderCommentRepository.save(systemComment);

        String newStatusRu = translateOrderStatus(order.getStatus().name());
        emailService.sendOrderStatusChangedEmail(savedOrder, oldStatusRu, newStatusRu);
        emailService.sendOrderReadyForReviewToReviewer(savedOrder);

        return mapToOrderFullDtoWithComments(savedOrder);
    }

    private OrderFullDto mapToOrderFullDtoWithComments(Order order) {
        List<OrderMaterialDto> materials = order.getMaterials().stream()
                .map(m -> new OrderMaterialDto(m.getMaterial().getName(),
                        m.getMaterial().getCategory().name(), m.getQuantity(), m.getPrice()))
                .collect(Collectors.toList());

        List<OrderFileDto> files = order.getFiles().stream()
                .map(f -> new OrderFileDto(f.getId(), f.getFileName(), f.getFileType(),
                        "/api/files/" + f.getId() + "/download"))
                .collect(Collectors.toList());

        List<OrderCommentDto> comments = orderCommentRepository
                .findByOrderIdOrderByCreatedAtAsc(order.getId())
                .stream()
                .map(c -> new OrderCommentDto(c.getId(), c.getUser().getId(),
                        c.getUser().getFullName(), c.getUser().getRole().name(),
                        c.getComment(), c.getIsSystem(), c.getCreatedAt()))
                .collect(Collectors.toList());

        Review review = order.getReview();
        ReviewDto reviewDto = review != null ? new ReviewDto(
                review.getId(), review.getOrder().getId(), review.getReviewer().getId(),
                review.getReviewer().getFullName(), review.getComment(),
                translateReviewStatus(review.getStatus()),
                translateOrderStatusAfterReview(review.getOrderStatusAfterReview()),
                review.getCreatedAt()) : null;

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
                order.getCreatedAt(),
                comments
        );
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

        List<OrderCommentDto> comments = orderCommentRepository
                .findByOrderIdOrderByCreatedAtAsc(order.getId())
                .stream()
                .map(c -> new OrderCommentDto(c.getId(), c.getUser().getId(),
                        c.getUser().getFullName(), c.getUser().getRole().name(),
                        c.getComment(), c.getIsSystem(), c.getCreatedAt()))
                .collect(Collectors.toList());

        Review review = order.getReview();
        ReviewDto reviewDto = null;

        if (review != null) {
            reviewDto = convertReviewToDto(review);
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
                order.getCreatedAt(),
                comments
        );
    }

    private ReviewDto convertReviewToDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getOrder().getId(),
                review.getReviewer().getId(),
                review.getReviewer().getFullName(),
                review.getComment(),
                translateReviewStatus(review.getStatus()),
                translateOrderStatusAfterReview(review.getOrderStatusAfterReview()),
                review.getCreatedAt()
        );
    }

    private String translateOrderStatusAfterReview(String status) {
        if (status == null) return null;

        return switch (status.toLowerCase()) {
            case "editing" -> "Редактируется";
            case "ready_for_print" -> "Готов к печати";
            case "canceled" -> "Отменён";
            default -> status;
        };
    }

    public OrderFullDto getOrderByReview(Long reviewId, User user) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getReviewer().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied - not the reviewer");
        }

        Order order = review.getOrder();

        return mapToOrderFullDto(order);
    }

    @Transactional
    public OrderFullDto addFiles(Long orderId, User author, List<MultipartFile> newFiles) {
        Order order = validateOrderForAuthor(orderId, author);

        for (MultipartFile file : newFiles) {
            OrderFile of = saveOrderFile(order, file);
            order.getFiles().add(of);
            orderFileRepo.save(of);
        }

        addSystemComment(order, author, "📎 Автор добавил новые файлы (" + newFiles.size() + " шт.)");
        return mapToOrderFullDtoWithComments(order);
    }

    @Transactional
    public OrderFullDto deleteFiles(Long orderId, User author, List<Long> fileIds) {
        Order order = validateOrderForAuthor(orderId, author);

        List<OrderFile> filesToDelete = order.getFiles().stream()
                .filter(f -> fileIds.contains(f.getId()))
                .collect(Collectors.toList());

        for (OrderFile file : filesToDelete) {
            try {
                Path path = Paths.get(file.getFilePath());
                Files.deleteIfExists(path);
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + file.getFilePath());
            }
            order.getFiles().remove(file);
            orderFileRepo.delete(file);
        }

        addSystemComment(order, author, "🗑 Автор удалил файлы (" + filesToDelete.size() + " шт.)");
        return mapToOrderFullDtoWithComments(order);
    }

    private Order validateOrderForAuthor(Long orderId, User author) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.Status.editing) {
            throw new RuntimeException("Действие доступно только в статусе 'Редактируется'");
        }
        if (!order.getUser().getId().equals(author.getId())) {
            throw new RuntimeException("Вы можете управлять только своими заказами");
        }
        return order;
    }


    private void addSystemComment(Order order, User user, String message) {
        OrderComment comment = new OrderComment(order, user, message, true);
        orderCommentRepository.save(comment);
    }

    private OrderFile saveOrderFile(Order order, MultipartFile file) {
        Path dir = Paths.get("uploads/orders/" + order.getId());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            originalName = "file_" + System.currentTimeMillis();
        }

        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex);
        }
        String storedFilename = UUID.randomUUID() + extension;
        Path path = dir.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OrderFile of = new OrderFile();
        of.setOrder(order);
        of.setFileName(originalName);
        of.setFileType(file.getContentType());
        of.setFilePath(path.toString());
        return of;
    }
}