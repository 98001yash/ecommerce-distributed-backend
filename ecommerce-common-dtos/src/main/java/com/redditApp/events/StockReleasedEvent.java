package com.redditApp.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReleasedEvent {

    private Long eventId;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private Long warehouseId;
    private Long timestamp;

}
