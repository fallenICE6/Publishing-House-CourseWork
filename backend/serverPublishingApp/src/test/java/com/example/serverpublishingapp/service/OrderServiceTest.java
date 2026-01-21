package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.config.OrderMapper;
import com.example.serverpublishingapp.dto.*;
import com.example.serverpublishingapp.entity.*;
import com.example.serverpublishingapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private OrderMaterialRepository orderMaterialRepo;

    @Mock
    private OrderFileRepository orderFileRepo;

    @Mock
    private MaterialRepository materialRepo;

    @Mock
    private PublishingServiceRepository serviceRepo;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private OrderMapper mapper;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private PublishingService publishingService;
    private Material material;
    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setMiddleName("Middle");
        user.setPhone("+123456789");

        publishingService = new PublishingService();
        publishingService.setId(1L);
        publishingService.setTitle("Printing Service");
        publishingService.setPrice(new BigDecimal("1000.00"));

        material = new Material();
        material.setId(1L);
        material.setName("Paper");
        material.setCategory(Material.Category.paper);
        material.setPrice(new BigDecimal("10.00"));

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setService(publishingService);
        order.setPages(100);
        order.setQuantity(50);
        order.setTotalPrice(new BigDecimal("6000.00"));
        order.setStatus(Order.Status.created);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        order.setMaterials(new ArrayList<>());
        order.setFiles(new ArrayList<>());

        orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setServiceName("Printing Service");
        orderDto.setPages(100);
        orderDto.setQuantity(50);
        orderDto.setTotalPrice(new BigDecimal("6000.00"));
    }

    @Test
    void create_ShouldCreateOrder_WithValidRequest() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setServiceId(1L);
        req.setPages(100);
        req.setQuantity(50);
        req.setEmail("test@example.com");

        Order newOrder = new Order();
        newOrder.setId(1L);
        newOrder.setUser(user);
        newOrder.setService(publishingService);
        newOrder.setPages(100);
        newOrder.setQuantity(50);
        newOrder.setTotalPrice(new BigDecimal("6000.00"));
        newOrder.setMaterials(new ArrayList<>());
        newOrder.setFiles(new ArrayList<>());

        when(serviceRepo.findById(1L)).thenReturn(Optional.of(publishingService));
        when(orderRepo.save(any(Order.class))).thenReturn(newOrder);
        when(mapper.toDto(any(Order.class))).thenReturn(orderDto);

        OrderDto result = orderService.create(req, List.of(), user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(serviceRepo).findById(1L);
        verify(orderRepo, atLeastOnce()).save(any(Order.class));
    }

    @Test
    void create_ShouldThrowException_WhenServiceNotFound() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setServiceId(99L);

        when(serviceRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> orderService.create(req, List.of(), user));
        verify(orderRepo, never()).save(any(Order.class));
    }

    @Test
    void getMyOrders_ShouldReturnUserOrders() {
        when(orderRepo.findByUserId(1L)).thenReturn(Arrays.asList(order));

        List<OrderFullDto> result = orderService.getMyOrders(user);

        assertNotNull(result);
        verify(orderRepo).findByUserId(1L);
    }

    @Test
    void translateOrderStatus_ShouldReturnCorrectTranslation() {
        assertEquals("Создан", orderService.translateOrderStatus("created"));
        assertEquals("На проверке", orderService.translateOrderStatus("under_review"));
        assertEquals("Редактируется", orderService.translateOrderStatus("editing"));
        assertEquals("Готов к печати", orderService.translateOrderStatus("ready_for_print"));
        assertEquals("Завершён", orderService.translateOrderStatus("completed"));
        assertEquals("Отменён", orderService.translateOrderStatus("canceled"));
        assertEquals("Неизвестно", orderService.translateOrderStatus("unknown"));
    }

    @Test
    void getOrderById_ShouldReturnOrder_WhenUserIsOwner() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderFullDto result = orderService.getOrderById(1L, user);

        assertNotNull(result);
        verify(orderRepo).findById(1L);
    }

    @Test
    void getOrderById_ShouldThrowException_WhenUserNotOwner() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class,
                () -> orderService.getOrderById(1L, otherUser));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders_WhenNoFilters() {
        when(orderRepo.findAll()).thenReturn(Arrays.asList(order));

        List<OrderFullDto> result = orderService.getAllOrders(null, null);

        assertNotNull(result);
        verify(orderRepo).findAll();
    }

    @Test
    void getAllOrders_ShouldFilterBySearch() {

        when(orderRepo.searchByOrderIdOrFullName("test")).thenReturn(Arrays.asList(order));


        List<OrderFullDto> result = orderService.getAllOrders("test", null);


        assertNotNull(result);
        verify(orderRepo).searchByOrderIdOrFullName("test");
    }

    @Test
    void getAllOrders_ShouldFilterByStatus() {

        order.setStatus(Order.Status.under_review);
        when(orderRepo.findAll()).thenReturn(Arrays.asList(order));


        List<OrderFullDto> result = orderService.getAllOrders(null, "under_review");

        assertNotNull(result);
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus_WhenTransitionValid() {

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("under_review");
        order.setStatus(Order.Status.created);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepo.save(any(Order.class))).thenReturn(order);


        OrderFullDto result = orderService.updateOrderStatus(1L, request, user);

        assertNotNull(result);
        assertEquals(Order.Status.under_review, order.getStatus());
        verify(orderRepo).save(order);
    }

    @Test
    void updateOrderStatus_ShouldThrowException_WhenInvalidTransition() {

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("editing");
        order.setStatus(Order.Status.created);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(1L, request, user));
        verify(orderRepo, never()).save(any(Order.class));
    }

    @Test
    void getOrderByIdAdmin_ShouldReturnOrder() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        OrderFullDto result = orderService.getOrderByIdAdmin(1L);

        assertNotNull(result);
        verify(orderRepo).findById(1L);
    }

    @Test
    void getOrdersForReview_ShouldReturnUnderReviewOrders() {

        when(orderRepo.findByStatus(Order.Status.under_review)).thenReturn(Arrays.asList(order));


        List<Order> result = orderService.getOrdersForReview();

        assertEquals(1, result.size());
        verify(orderRepo).findByStatus(Order.Status.under_review);
    }
}