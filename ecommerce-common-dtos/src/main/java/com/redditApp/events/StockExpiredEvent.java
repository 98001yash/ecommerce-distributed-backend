package com.redditApp.events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExpiredEvent {

    private Long eventId;
    private Long reservationId;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private Long timestamp;

}
