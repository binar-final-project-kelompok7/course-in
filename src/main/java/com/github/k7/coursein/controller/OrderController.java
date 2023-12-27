package com.github.k7.coursein.controller;

import com.github.k7.coursein.model.CreateOrderRequest;
import com.github.k7.coursein.model.DashboardResponse;
import com.github.k7.coursein.model.OrderResponse;
import com.github.k7.coursein.model.PagingResponse;
import com.github.k7.coursein.model.WebResponse;
import com.github.k7.coursein.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<OrderResponse>> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(WebResponse.<OrderResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .data(orderResponse)
                .build());
    }

    @GetMapping(
        path = "/dashboard",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<DashboardResponse>> getDashboardOrders(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<DashboardResponse> allOrder = orderService.getDashboardOrders(page, size);
        return WebResponse.<List<DashboardResponse>>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(allOrder.getContent())
            .paging(PagingResponse.builder()
                .currentPage(allOrder.getNumber())
                .totalPage(allOrder.getTotalPages())
                .size(allOrder.getSize())
                .build())
            .build();
    }

}
