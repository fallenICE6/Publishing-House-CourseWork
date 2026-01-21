package com.example.serverpublishingapp.service;

import com.example.serverpublishingapp.dto.CreateReviewRequest;
import com.example.serverpublishingapp.dto.OrderForReviewDto;
import com.example.serverpublishingapp.dto.OrderFileDto;
import com.example.serverpublishingapp.dto.ReviewDto;
import com.example.serverpublishingapp.entity.Order;
import com.example.serverpublishingapp.entity.PublishingService;
import com.example.serverpublishingapp.entity.Review;
import com.example.serverpublishingapp.entity.User;
import com.example.serverpublishingapp.repository.OrderRepository;
import com.example.serverpublishingapp.repository.ReviewRepository;
import com.example.serverpublishingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ReviewService reviewService;

    private User reviewer;
    private Order order;
    private Review review;

    @BeforeEach
    void setUp() {
        reviewer = new User();
        reviewer.setId(1L);
        reviewer.setUsername("reviewer");
        reviewer.setFirstName("Review");
        reviewer.setLastName("User");

        order = new Order();
        order.setId(1L);
        order.setStatus(Order.Status.under_review);
        order.setTotalPrice(new BigDecimal("5000.00"));
        order.setCreatedAt(LocalDateTime.now());

        review = new Review();
        review.setId(1L);
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setComment("Good work");
        review.setStatus("approved");
        review.setOrderStatusAfterReview("ready_for_print");
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReview_ShouldCreateReview_WhenValidApprove() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setComment("Good work");
        request.setDecision("approve");
        request.setOrderStatusAfterReview("ready_for_print");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDto result = reviewService.createReview(1L, "reviewer", request);

        assertNotNull(result);
        assertEquals("approved", result.getStatus());
        verify(reviewRepository).save(any(Review.class));
        verify(orderRepository).save(order);
    }

    @Test
    void createReview_ShouldCreateReview_WhenValidReject() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setComment("Needs improvement");
        request.setDecision("reject");
        request.setOrderStatusAfterReview("canceled");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewDto result = reviewService.createReview(1L, "reviewer", request);

        assertNotNull(result);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenOrderNotUnderReview() {
        order.setStatus(Order.Status.created);
        CreateReviewRequest request = new CreateReviewRequest();
        request.setDecision("approve");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class,
                () -> reviewService.createReview(1L, "reviewer", request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenReviewAlreadyExists() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setDecision("approve");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> reviewService.createReview(1L, "reviewer", request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenInvalidDecision() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setDecision("invalid");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByOrderId(1L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> reviewService.createReview(1L, "reviewer", request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getOrdersAvailableForReview_ShouldReturnOrders() {
        reviewer.setId(1L);
        when(userRepository.findByUsername("reviewer"))
                .thenReturn(Optional.of(reviewer));

        User customer = new User();
        customer.setId(10L);
        customer.setFirstName("Ivan");
        customer.setLastName("Petrov");

        order.setId(100L);
        order.setUser(customer);
        order.setStatus(Order.Status.under_review);
        order.setPages(120);
        order.setQuantity(2);
        order.setTotalPrice(new BigDecimal("1999.99"));
        order.setCreatedAt(LocalDateTime.now());
        order.setFiles(new ArrayList<>());

        PublishingService service = new PublishingService();
        service.setTitle("Book Editing");
        order.setService(service);

        when(orderRepository.findByStatus(Order.Status.under_review))
                .thenReturn(List.of(order));

        when(orderService.translateOrderStatus(anyString()))
                .thenReturn("На проверке");

        List<OrderForReviewDto> result =
                reviewService.getOrdersAvailableForReview("reviewer");

        assertNotNull(result);
        assertEquals(1, result.size());

        OrderForReviewDto dto = result.get(0);
        assertEquals("Ivan Petrov", dto.getCustomerName());
        assertEquals("Book Editing", dto.getServiceTitle());
        assertEquals(120, dto.getPages());
        assertEquals(2, dto.getQuantity());
        assertEquals("На проверке", dto.getStatus());
        assertFalse(dto.isHasReview());

        verify(orderRepository).findByStatus(Order.Status.under_review);
    }



    @Test
    void getReviewsByReviewer_ShouldReturnReviewerReviews() {
        when(userRepository.findByUsername("reviewer")).thenReturn(Optional.of(reviewer));
        when(reviewRepository.findByReviewer(reviewer)).thenReturn(Arrays.asList(review));

        List<ReviewDto> result = reviewService.getReviewsByReviewer("reviewer");

        assertEquals(1, result.size());
        verify(reviewRepository).findByReviewer(reviewer);
    }

    @Test
    void translateReviewStatus_ShouldReturnCorrectTranslation() {
        assertEquals("На доработку", reviewService.translateReviewStatus("pending"));
        assertEquals("Одобрено", reviewService.translateReviewStatus("approved"));
        assertEquals("Отклонено", reviewService.translateReviewStatus("rejected"));
        assertEquals("—", reviewService.translateReviewStatus(null));
        assertEquals("unknown", reviewService.translateReviewStatus("unknown"));
    }

    @Test
    void translateOrderStatus_ShouldReturnCorrectTranslation() {
        assertEquals("Редактируется", reviewService.translateOrderStatus("editing"));
        assertEquals("Готов к печати", reviewService.translateOrderStatus("ready_for_print"));
        assertEquals("Отменён", reviewService.translateOrderStatus("canceled"));
        assertEquals("—", reviewService.translateOrderStatus(null));
        assertEquals("unknown", reviewService.translateOrderStatus("unknown"));
    }
}