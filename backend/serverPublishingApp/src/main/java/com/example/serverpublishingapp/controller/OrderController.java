package com.example.serverpublishingapp.controller;

import com.example.serverpublishingapp.dto.*;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public OrderDto create(
            @RequestPart("order") CreateOrderRequest order,
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal User user
    ) {
        return orderService.create(order, files, user);
    }

    @GetMapping("/my")
    public List<OrderFullDto> myOrders(@AuthenticationPrincipal User user) {
        return orderService.getMyOrders(user);
    }

    @GetMapping("/{id}")
    public OrderFullDto getOrderById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return orderService.getOrderById(id, user);
    }

    @GetMapping("/admin/all")
    public List<OrderFullDto> getAllOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        return orderService.getAllOrders(search, status);
    }

    @PutMapping("/admin/{id}/status")
    public OrderFullDto updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal User admin
    ) {
        return orderService.updateOrderStatus(id, request, admin);
    }

    @GetMapping("/admin/{id}")
    public OrderFullDto getOrderByIdAdmin(@PathVariable Long id) {
        return orderService.getOrderByIdAdmin(id);
    }

    @GetMapping("/by-review/{reviewId}")
    public OrderFullDto getOrderByReview(@PathVariable Long reviewId,
                                         @AuthenticationPrincipal User user) {
        return orderService.getOrderByReview(reviewId, user);
    }

    @GetMapping("/editor/editing-orders")
    @PreAuthorize("hasRole('EDITOR')")
    public ResponseEntity<List<OrderFullDto>> getEditingOrdersForEditor() {
        return ResponseEntity.ok(orderService.getEditingOrdersForEditor());
    }

    @PostMapping("/editor/{orderId}/comment")
    @PreAuthorize("hasRole('EDITOR')")
    public ResponseEntity<OrderCommentDto> addEditorComment(
            @PathVariable Long orderId,
            @Valid @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal User editor) {
        return ResponseEntity.ok(orderService.addCommentToEditingOrder(orderId, editor, request.getComment()));
    }

    @PostMapping("/editor/{orderId}/send-to-review")
    @PreAuthorize("hasRole('EDITOR')")
    public ResponseEntity<OrderFullDto> sendToReview(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User editor) {
        return ResponseEntity.ok(orderService.sendToReview(orderId, editor));
    }

    @PostMapping("/author/{orderId}/comment")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<OrderCommentDto> addAuthorComment(
            @PathVariable Long orderId,
            @Valid @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal User author) {
        return ResponseEntity.ok(orderService.addCommentToEditingOrder(orderId, author, request.getComment()));
    }

    @PostMapping(value = "/author/{orderId}/reupload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<OrderFullDto> reuploadFiles(
            @PathVariable Long orderId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart(value = "comment", required = false) ReuploadFilesRequest request,
            @AuthenticationPrincipal User author) {
        String comment = request != null ? request.getComment() : null;
        return ResponseEntity.ok(orderService.reuploadFiles(orderId, author, files, comment));
    }

    @GetMapping("/{orderId}/comments")
    @PreAuthorize("hasAnyRole('AUTHOR', 'EDITOR', 'REVIEWER', 'ADMIN')")
    public ResponseEntity<List<OrderCommentDto>> getComments(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getCommentsByOrderId(orderId));
    }
    @GetMapping("/editor/{id}")
    @PreAuthorize("hasRole('EDITOR')")
    public ResponseEntity<OrderFullDto> getOrderByIdForEditor(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdForEditor(id));
    }

    @PostMapping(value = "/author/{orderId}/add-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<OrderFullDto> addFiles(
            @PathVariable Long orderId,
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal User author) {
        return ResponseEntity.ok(orderService.addFiles(orderId, author, files));
    }

    @DeleteMapping("/author/{orderId}/files")
    @PreAuthorize("hasRole('AUTHOR')")
    public ResponseEntity<OrderFullDto> deleteFiles(
            @PathVariable Long orderId,
            @RequestParam("ids") List<Long> fileIds,
            @AuthenticationPrincipal User author) {
        return ResponseEntity.ok(orderService.deleteFiles(orderId, author, fileIds));
    }

}
