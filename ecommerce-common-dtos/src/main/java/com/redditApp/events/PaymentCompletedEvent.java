package com.redditApp.events;

import com.redditApp.BaseEvent;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent extends BaseEvent {

    private Long orderId;
    private Long paymentId;
    private Long userId;
    private Double amount;

    private Instant completedAt;
}