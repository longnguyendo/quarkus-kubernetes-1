package org.lab.dev.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.lab.dev.domain.OrderItem;
import org.lab.dev.repository.OrderItemRepository;
import org.lab.dev.repository.OrderRepository;
import org.lab.dev.repository.ProductRepository;
import org.lab.dev.web.dto.OrderItemDto;

@Slf4j
@ApplicationScoped
@Transactional
public class OrderItemService {

    @Inject
    OrderItemRepository orderItemRepository;
    @Inject
    OrderRepository orderRepository;
    @Inject
    ProductRepository productRepository;

    public static OrderItemDto mapToDo(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getId(),
                orderItem.getQuantity(),
                orderItem.getProduct().getId(),
                orderItem.getOrder().getId()
        );
    }

    public OrderItemDto findById(Long id) {
        log.debug("Request find orderItem id : {}", id);
        return this.orderItemRepository.findById(id)
                .map(OrderItemService::mapToDo)
                .orElse(null);
    }

    public OrderItemDto create(OrderItemDto orderItemDto) {
        log.debug("Request create new orderItem : {}",orderItemDto);
        var order = this.orderRepository
                .findById(orderItemDto.getOrderId())
                .orElseThrow(() ->
                        new IllegalStateException("The order does not exist!"));
        var product = this.productRepository
                .findById(orderItemDto.getProductId())
                .orElseThrow(() ->
                        new IllegalStateException("Product does not exist!"));
        var orderItem = this.orderItemRepository.save
                (new OrderItem(
                        orderItemDto.getQuantity(),
                        product,
                        order
                ));
        order.setPrice(order.getPrice().add(orderItem.getProduct().getPrice()));
        this.orderRepository.save(order);

        return mapToDo(orderItem);
    }

    public void delete(Long id) {
        log.debug("Request delete orderItem id: {}", id);

        var orderItem = this.orderItemRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("This orderItem does not exist to delete!"));
        var order = orderItem.getOrder();
        order.setPrice(order.getPrice().subtract(orderItem.getProduct().getPrice()));

        this.orderItemRepository.deleteById(id);

        order.getOrderItems().remove(orderItem);

        this.orderRepository.save(order);
    }
}
