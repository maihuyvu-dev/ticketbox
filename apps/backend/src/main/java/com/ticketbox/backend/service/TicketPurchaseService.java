package com.ticketbox.backend.service;

import com.ticketbox.backend.entity.*;
import com.ticketbox.backend.pattern.factory.TicketFactory;
import com.ticketbox.backend.pattern.factory.TicketFactoryProvider;
import com.ticketbox.backend.pattern.state.OrderStateContext;
import com.ticketbox.backend.pattern.strategy.PricingStrategy;
import com.ticketbox.backend.pattern.strategy.StandardPricingStrategy;
import com.ticketbox.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TicketPurchaseService {

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderStateContext orderStateContext;

    @Autowired
    private TicketFactoryProvider ticketFactoryProvider;

    /**
     * Handles ticket purchase with Concurrency & Idempotency protection
     */
    @Transactional
    public Order purchaseTicket(User user, Long categoryId, int quantity, String idempotencyKey) {

        // 1. Idempotency Check
        String idempKeyRedis = "idemp:" + idempotencyKey;
        if (!redisService.setIfAbsentIdempotencyKey(idempKeyRedis, Duration.ofMinutes(10))) {
            // Already processed or processing. In a real system, we might return the existing order.
            return orderRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Order is processing"));
        }

        // 2. Distributed Lock per user & category to prevent spamming
        String lockKey = "lock:purchase:" + user.getId() + ":" + categoryId;
        if (!redisService.acquireLock(lockKey, Duration.ofSeconds(10))) {
            throw new IllegalStateException("Too many requests. Please try again later.");
        }

        try {
            // 3. Pessimistic Locking on Database to prevent oversell
            TicketCategory category = ticketCategoryRepository.findByIdWithPessimisticLock(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            if (category.getAvailableQuantity() < quantity) {
                throw new IllegalStateException("Not enough tickets available. Oversell prevented.");
            }

            // Update quantity
            category.setAvailableQuantity(category.getAvailableQuantity() - quantity);
            ticketCategoryRepository.save(category);

            // 4. Calculate Price (Strategy Pattern)
            PricingStrategy pricingStrategy = new StandardPricingStrategy();
            BigDecimal unitPrice = pricingStrategy.calculatePrice(category.getPrice());
            BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantity));

            // 5. Create Order (State Pattern)
            Order order = Order.builder()
                    .user(user)
                    .totalAmount(totalPrice)
                    .status(OrderStatus.PENDING)
                    .idempotencyKey(idempotencyKey)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            order = orderRepository.save(order);
            
            // Move order state to Completed for simplicity in this flow, 
            // normally would be Paying -> wait for gateway -> Completed
            orderStateContext.processPayment(order);
            orderStateContext.completeOrder(order);
            order = orderRepository.save(order);

            // 6. Create OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .ticketCategory(category)
                    .quantity(quantity)
                    .price(unitPrice)
                    .build();
            orderItemRepository.save(orderItem);

            // 7. Create Tickets (Abstract Factory Pattern)
            // Lấy ra factory tương ứng dựa vào tên của TicketCategory
            TicketFactory factory = ticketFactoryProvider.getFactory(category.getName());
            
            for (int i = 0; i < quantity; i++) {
                Ticket ticket = factory.createTicket(category, user, order);
                ticketRepository.save(ticket);
            }

            return order;

        } finally {
            // 8. Release the distributed lock
            redisService.releaseLock(lockKey);
        }
    }
}
