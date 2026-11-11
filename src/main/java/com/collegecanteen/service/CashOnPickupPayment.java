package com.collegecanteen.service;

import com.collegecanteen.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CashOnPickupPayment implements PaymentProcessor {
    @Override
    public Payment processPayment(int orderId, double amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }
}
