package com.collegecanteen.service;

import com.collegecanteen.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

public class DemoOnlinePayment implements PaymentProcessor {
    private final Random random = new Random();

    @Override
    public Payment processPayment(int orderId, double amount, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(random.nextBoolean() ? "SUCCESS" : "FAILED");
        return payment;
    }
}
