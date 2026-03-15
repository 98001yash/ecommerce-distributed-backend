package com.redditApp.events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockReservedEvent {

    private long eventId;
    private Long orderId;
    private Long productId;

    private Integer quantity;
    private Long warehouseId;

    private Long userId;
    private Long timestamp;
}
