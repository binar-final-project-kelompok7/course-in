package com.github.k7.coursein.controller;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.CourseFilter;
import com.github.k7.coursein.enums.CourseLevel;
import com.github.k7.coursein.enums.CourseType;
import com.github.k7.coursein.model.CourseResponse;
import com.github.k7.coursein.model.DeleteUserRequest;
import com.github.k7.coursein.model.RegisterOTPResponse;
import com.github.k7.coursein.model.OrderResponse;
import com.github.k7.coursein.model.PagingResponse;
import com.github.k7.coursein.model.PayOrderRequest;
import com.github.k7.coursein.model.RegisterUserRequest;
import com.github.k7.coursein.model.ResendOTPRequest;
import com.github.k7.coursein.model.UpdatePasswordUserRequest;
import com.github.k7.coursein.model.UpdateUserRequest;
import com.github.k7.coursein.model.UserResponse;
import com.github.k7.coursein.model.VerifyOtpRequest;
import com.github.k7.coursein.model.WebResponse;
import com.github.k7.coursein.service.CourseService;
import com.github.k7.coursein.service.OrderService;
import com.github.k7.coursein.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    private final OrderService orderService;

    private final CourseService courseService;

    @PostMapping(
        path = "/register",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RegisterOTPResponse> registerUser(@RequestBody RegisterUserRequest request) {
        RegisterOTPResponse response = userService.registerUser(request);
        return WebResponse.<RegisterOTPResponse>builder()
            .code(HttpStatus.CREATED.value())
            .message(HttpStatus.CREATED.getReasonPhrase())
            .data(response)
            .build();
    }

    @PostMapping(
        path = "/verify-otp",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebResponse<String>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        String token = userService.verifyOTP(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .body(WebResponse.<String>builder()
                .code(HttpStatus.CREATED.value())
                .message(HttpStatus.CREATED.getReasonPhrase())
                .build());
    }

    @PostMapping(
        path = "/resend-otp",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<RegisterOTPResponse> resendOtp(@RequestBody ResendOTPRequest request) {
        RegisterOTPResponse response = userService.resendOtp(request);
        return WebResponse.<RegisterOTPResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(response)
            .build();
    }

    @GetMapping(
        path = "/{username}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> getUser(@PathVariable("username") String username) {
        UserResponse response = userService.getUser(username);
        return WebResponse.<UserResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(response)
            .build();
    }

    @PatchMapping(
        path = "/{username}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> updateUser(@PathVariable("username") String username,
                                                @RequestBody UpdateUserRequest request) {
        UserResponse userResponse = userService.updateUser(username, request);
        return WebResponse.<UserResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(userResponse)
            .build();
    }

    @PutMapping(
        path = "/update-password/{username}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> updatePassword(@PathVariable("username") String username,
                                              @RequestBody UpdatePasswordUserRequest request) {
        userService.updatePassword(username, request);
        return WebResponse.<String>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .build();
    }

    @DeleteMapping(
        path = "/delete/{username}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> deleteUser(@PathVariable("username") String username,
                                          @RequestBody DeleteUserRequest request) {
        userService.deleteUser(username, request);
        return WebResponse.<String>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .build();
    }

    @GetMapping(
        path = "/count",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Long> countUser() {
        long count = userService.countUser();
        return WebResponse.<Long>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(count)
            .build();
    }

    @GetMapping(
        path = "/{username}/courses",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<CourseResponse>> getAllCourse(
        @PathVariable("username") String username,
        @RequestParam(name = "type", required = false) CourseType type,
        @RequestParam(name = "filters", required = false) Set<CourseFilter> filters,
        @RequestParam(name = "categories", required = false) Set<CourseCategory> categories,
        @RequestParam(name = "levels", required = false) Set<CourseLevel> levels,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<CourseResponse> allCourse = courseService.getAllCourseUser(
            username, type, filters, categories, levels, page, size);
        return WebResponse.<List<CourseResponse>>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(allCourse.getContent())
            .paging(PagingResponse.builder()
                .currentPage(allCourse.getNumber())
                .totalPage(allCourse.getTotalPages())
                .size(allCourse.getSize())
                .build())
            .build();
    }

    @GetMapping(
        path = "/{username}/orders",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<OrderResponse>> getOrders(
        @PathVariable("username") String username,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<OrderResponse> allOrder = orderService.getAllOrder(username, page, size);
        return WebResponse.<List<OrderResponse>>builder()
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

    @GetMapping(
        path = "{username}/orders/{orderId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderResponse> getOrder(@PathVariable("username") String username,
                                               @PathVariable("orderId") String orderId) {
        OrderResponse orderResponse = orderService.getOrder(username, orderId);
        return WebResponse.<OrderResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(orderResponse)
            .build();
    }

    @PatchMapping(
        path = "{username}/orders/{orderId}/pay",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<OrderResponse> payOrder(@PathVariable("username") String username,
                                               @PathVariable("orderId") String orderId,
                                               @RequestBody PayOrderRequest request) {
        OrderResponse orderResponse = orderService.payOrder(username, orderId, request);
        return WebResponse.<OrderResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.getReasonPhrase())
            .data(orderResponse)
            .build();
    }

}
