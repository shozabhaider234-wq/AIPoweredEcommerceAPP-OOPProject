package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.OrderDto;
import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;

abstract public class OrderServiceAbstraction {
    abstract public OrderDto placeOrder();
    abstract public OrderDto cancelOrder(Long orderId);
    abstract public OrderDto getOrderById(Long orderId);
    abstract public PagedResponse<OrderDto> getOrderHistory(int page, int size);
}
