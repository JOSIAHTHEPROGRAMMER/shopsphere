package com.app.shopsphere.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.shopsphere.dto.admin.AdminDashboardResponse;
import com.app.shopsphere.dto.admin.AdminRevenueResponse;
import com.app.shopsphere.dto.admin.BestSellingProductResponse;
import com.app.shopsphere.dto.product.ProductResponse;
import com.app.shopsphere.enum_values.OrderStatus;
import com.app.shopsphere.model.Order;
import com.app.shopsphere.model.OrderItem;
import com.app.shopsphere.model.Product;
import com.app.shopsphere.repository.OrderRepository;
import com.app.shopsphere.repository.ProductRepository;
import com.app.shopsphere.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Exposes dashboard and reporting data for administrative review of catalog and
 * order activity.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;

    /**
     * Builds the administrative dashboard summary for products, users, orders, and
     * stock health.
     *
     * @return the dashboard aggregates for the current catalog and order state
     */
    @Transactional
    public AdminDashboardResponse getDashboard() {

        AdminDashboardResponse res = new AdminDashboardResponse();

        res.setTotalUsers(userRepository.count());
        res.setTotalProducts(productRepository.count());
        res.setTotalOrders(orderRepository.count());
        res.setPendingOrders(orderRepository.countByStatus(OrderStatus.PENDING));

        List<Order> completedOrders = orderRepository.findByStatusNot(OrderStatus.CANCELLED);

        BigDecimal revenue = completedOrders.stream()
                .map(order -> order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b))
                .setScale(2, RoundingMode.HALF_UP);

        res.setRevenue(revenue);

        res.setProductsInStock(productRepository.countByStockQuantityGreaterThan(0));
        res.setOutOfStock(productRepository.countByStockQuantity(0));
        res.setLowStock(productRepository.countByStockQuantityBetween(1, DEFAULT_LOW_STOCK_THRESHOLD));

        return res;
    }

    /**
     * Calculates revenue totals for the requested time window.
     *
     * @param from the inclusive start of the reporting window
     * @param to   the inclusive end of the reporting window
     * @return the revenue metrics for completed orders in the selected range
     */
    public AdminRevenueResponse getRevenue(LocalDateTime from, LocalDateTime to) {

        List<Order> orders = (from != null && to != null)
                ? orderRepository.findByStatusNotAndCreatedAtBetween(OrderStatus.CANCELLED, from, to)
                : orderRepository.findByStatusNot(OrderStatus.CANCELLED);

        BigDecimal totalRevenue = orders.stream()
                .map(order -> order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b))
                .setScale(2, RoundingMode.HALF_UP);

        int orderCount = orders.size();

        BigDecimal averageOrderValue = orderCount == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);

        AdminRevenueResponse res = new AdminRevenueResponse();

        res.setTotalRevenue(totalRevenue);
        res.setOrderCount(orderCount);
        res.setAverageOrderValue(averageOrderValue);
        res.setFrom(from);
        res.setTo(to);

        return res;
    }

    /**
     * Computes the most frequently purchased products for the admin reporting view.
     *
     * @param limit the maximum number of products to return
     * @return the ranked product sales summary
     */
    @Transactional
    public List<BestSellingProductResponse> getBestSelling(int limit) {

        List<Order> orders = orderRepository.findByStatusNot(OrderStatus.CANCELLED);

        Map<Long, BestSellingProductResponse> statsByProduct = new HashMap<>();

        for (Order order : orders) {

            for (OrderItem item : order.getItems()) {

                Product product = item.getProduct();

                BestSellingProductResponse entry = statsByProduct.get(product.getId());

                if (entry == null) {
                    entry = new BestSellingProductResponse();
                    entry.setProductId(String.valueOf(product.getId()));
                    entry.setProductName(product.getName());
                    entry.setUnitsSold(0);
                    entry.setRevenue(BigDecimal.ZERO);
                    statsByProduct.put(product.getId(), entry);
                }

                entry.setUnitsSold(entry.getUnitsSold() + item.getQuantity());
                entry.setRevenue(
                        entry.getRevenue().add(
                                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
            }
        }

        return statsByProduct.values()
                .stream()
                .sorted((a, b) -> b.getUnitsSold().compareTo(a.getUnitsSold()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Returns products that are below the provided stock threshold.
     *
     * @param threshold the maximum stock quantity to include in the result
     * @return the low inventory products for admin review
     */
    public List<ProductResponse> getLowStock(int threshold) {

        return productRepository.findByStockQuantityBetween(1, threshold)
                .stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToProductResponse(Product product) {

        ProductResponse res = new ProductResponse();

        res.setId(String.valueOf(product.getId()));
        res.setName(product.getName());
        res.setDescription(product.getDescription());
        res.setPrice(product.getPrice());
        res.setStockQuantity(product.getStockQuantity());
        res.setImageUrl(product.getImageUrl());
        res.setCategory(product.getCategory());
        res.setActive(product.getActive());

        return res;
    }
}