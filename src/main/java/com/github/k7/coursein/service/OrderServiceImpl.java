package com.github.k7.coursein.service;

import com.github.k7.coursein.entity.Course;
import com.github.k7.coursein.entity.Order;
import com.github.k7.coursein.entity.User;
import com.github.k7.coursein.enums.OrderStatus;
import com.github.k7.coursein.generator.OrderIdGenerator;
import com.github.k7.coursein.model.CreateOrderRequest;
import com.github.k7.coursein.model.DashboardResponse;
import com.github.k7.coursein.model.OrderResponse;
import com.github.k7.coursein.model.PayOrderRequest;
import com.github.k7.coursein.repository.CourseRepository;
import com.github.k7.coursein.repository.OrderRepository;
import com.github.k7.coursein.repository.UserRepository;
import com.github.k7.coursein.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;

    private final CourseRepository courseRepository;

    private final OrderRepository orderRepository;

    private final ValidationService validationService;

    private static final String ORDER_NOT_FOUND = "Order not found!";

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        validationService.validate(request);

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        Course course = courseRepository.findByCode(request.getCourseCode())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found!"));

        if (orderRepository.existsByUserAndCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course already bought!");
        }

        Double ppn = countPpn(course);
        Double totalTransfer = course.getPrice() + ppn;

        Order order = Order.builder()
            .id(OrderIdGenerator.generateOrderId())
            .status(OrderStatus.PROCESSING)
            .createdAt(TimeUtil.getFormattedLocalDateTimeNow())
            .user(user)
            .course(course)
            .totalTransfer(totalTransfer)
            .build();

        orderRepository.save(order);

        return toOrderResponse(order);
    }

    private static Double countPpn(Course course) {
        Double ppnRate = 11.0;
        return (course.getPrice() * ppnRate) / 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrder(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders = orderRepository.findAllByUser(user, pageable);

        List<OrderResponse> orderResponses = orders.getContent().stream()
            .map(OrderServiceImpl::toOrderResponse)
            .collect(Collectors.toList());

        return new PageImpl<>(orderResponses, pageable, orders.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String username, String id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND));

        return toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DashboardResponse> getDashboardOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAll(pageable);

        List<DashboardResponse> orderResponses = orders.getContent().stream()
            .map(order ->
                OrderServiceImpl.toDashboardResponse(
                    order.getUser(),
                    order.getCourse(),
                    order
                ))
            .collect(Collectors.toList());

        return new PageImpl<>(orderResponses, pageable, orders.getTotalElements());
    }

    @Override
    @Transactional
    public OrderResponse payOrder(String username, String orderId, PayOrderRequest request) {
        validationService.validate(request);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND));

        Course course = order.getCourse();

        if (!(order.getUser().getUsername().equals(user.getUsername()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ORDER_NOT_FOUND);
        }

        if (!order.getStatus().equals(OrderStatus.PROCESSING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is completed!");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCompletedAt(TimeUtil.getFormattedLocalDateTimeNow());
        orderRepository.save(order);

        user.getCourses().add(course);
        userRepository.save(user);

        return toOrderResponse(order);
    }

    private static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
            .orderCode(order.getId())
            .username(order.getUser().getUsername())
            .courseName(order.getCourse().getName())
            .createdAt(TimeUtil.formatToString(order.getCreatedAt()))
            .completedAt(TimeUtil.formatToString(order.getCompletedAt()))
            .paymentMethod(order.getPaymentMethod())
            .status(order.getStatus())
            .totalPrice(order.getCourse().getPrice())
            .ppn(countPpn(order.getCourse()))
            .totalTransfer(order.getTotalTransfer())
            .build();
    }

    private static DashboardResponse toDashboardResponse(User user, Course course, Order order) {
        return DashboardResponse.builder()
            .username(user.getUsername())
            .category(course.getCategory())
            .courseName(course.getName())
            .status(order.getStatus())
            .completedAt(TimeUtil.formatToString(order.getCompletedAt()))
            .build();
    }

}
