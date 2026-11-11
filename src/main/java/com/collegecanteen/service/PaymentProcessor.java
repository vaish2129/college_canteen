package com.collegecanteen.service;

import com.collegecanteen.model.Payment;

public interface PaymentProcessor {
    Payment processPayment(int orderId, double amount, String paymentMethod);
}
