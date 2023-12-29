package com.github.k7.coursein.entity;

import com.github.k7.coursein.enums.OrderPaymentMethod;
import com.github.k7.coursein.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
@Builder
@ToString(exclude = {"user", "course"})
@EqualsAndHashCode(exclude = {"user", "course"})
public class Order {

    @Id
    private String id;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private OrderPaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false, name = "course_id", referencedColumnName = "id")
    private Course course;

    @Column(name = "total_transfer", nullable = false)
    private Double totalTransfer;

}
