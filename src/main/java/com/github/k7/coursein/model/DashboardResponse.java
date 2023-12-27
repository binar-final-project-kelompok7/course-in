package com.github.k7.coursein.model;

import com.github.k7.coursein.enums.CourseCategory;
import com.github.k7.coursein.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {

    private String username;

    private CourseCategory category;

    private String courseName;

    private OrderStatus status;

    private String paymentMethod;

    private String completedAt;

}
