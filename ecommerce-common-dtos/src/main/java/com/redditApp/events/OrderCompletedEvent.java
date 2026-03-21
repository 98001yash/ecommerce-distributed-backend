package com.redditApp.events;

import com.redditApp.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent extends BaseEvent {

    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Instant completedAt;
}