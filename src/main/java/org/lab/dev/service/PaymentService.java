package org.lab.dev.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.lab.dev.domain.Order;
import org.lab.dev.domain.Payment;
import org.lab.dev.domain.enums.OrderStatus;
import org.lab.dev.domain.enums.PaymentStatus;
import org.lab.dev.repository.OrderRepository;
import org.lab.dev.repository.PaymentRepository;
import org.lab.dev.web.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Transactional
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;
    @Inject
    OrderRepository orderRepository;

    public static PaymentDto mapToDto(Payment payment, Long orderId) {
        if (payment != null) {
            return new PaymentDto(
                    payment.getId(),
                    payment.getPaypalPaymentId(),
                    payment.getStatus().name(),
                    orderId
            );
        }
        return null;
    }

    public List<PaymentDto> findByPriceRange(Double max) {
        return this.paymentRepository
                .findAllByAmountBetween(BigDecimal.ZERO, BigDecimal.valueOf(max))
                .stream()
                .map(payment -> mapToDto(payment, findOrderPaymentId(payment.getId()).getId()))
                .collect(Collectors.toList());
    }

    public List<PaymentDto> findAll() {
        log.debug("Request to find all payments");
        return this.paymentRepository
                .findAll().stream().map(payment -> findById(payment.getId()))
                .collect(Collectors.toList());
    }

    public PaymentDto findById(Long id) {
        log.debug("Request to find by id: {}", id);
        var order = findOrderPaymentId(id);
        return this.paymentRepository
                .findById(id)
                .map(payment -> mapToDto(payment, order.getId()))
                .orElse(null);
    }

    public Order findOrderPaymentId(Long id) {
        return this.orderRepository.findByPaymentId(id)
                .orElseThrow(() -> new IllegalStateException("No order exists for payment Id" + id));
    }

    public PaymentDto create(PaymentDto paymentDto) {
        log.debug("Request to add new payment");

        var order = this.orderRepository.findById(paymentDto.getOrderId())
                .orElseThrow(() -> new IllegalStateException("The order does not exist!"));
        order.setStatus(OrderStatus.PAID);
        var payment = this.paymentRepository.saveAndFlush(new Payment(
                paymentDto.getPaypalPaymentId(),
                PaymentStatus.valueOf(paymentDto.getStatus()),
                order.getPrice()
        ));

        this.orderRepository.saveAndFlush(order);

        return mapToDto(payment, order.getId());
    }

    public void delete (Long id) {
        log.debug("Request to delete payment id: {}", id);
        this.paymentRepository.deleteById(id);
    }
}
