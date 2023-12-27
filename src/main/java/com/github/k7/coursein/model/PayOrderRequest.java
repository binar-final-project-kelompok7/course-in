package com.github.k7.coursein.model;

import com.github.k7.coursein.enums.OrderPaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayOrderRequest {

    @NotNull
    private OrderPaymentMethod paymentMethod;

}
