package com.github.k7.coursein.model;

import com.github.k7.coursein.enums.OrderPaymentMethod;
import com.github.k7.coursein.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private String orderCode;

    private String username;

    private String courseName;

    private String createdAt;

    private String completedAt;

    private OrderPaymentMethod paymentMethod;

    private OrderStatus status;

    private Double totalPrice;

    private Double ppn;

    private Double totalTransfer;

}
