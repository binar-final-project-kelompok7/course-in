package com.github.k7.coursein.service;

import com.github.k7.coursein.model.CreateOrderRequest;
import com.github.k7.coursein.model.DashboardResponse;
import com.github.k7.coursein.model.OrderResponse;
import com.github.k7.coursein.model.PayOrderRequest;
import org.springframework.data.domain.Page;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    Page<OrderResponse> getAllOrder(String username, int page, int size);

    OrderResponse getOrder(String username, String id);

    Page<DashboardResponse> getDashboardOrders(int page, int size);

    OrderResponse payOrder(String username, String orderId, PayOrderRequest request);

}
