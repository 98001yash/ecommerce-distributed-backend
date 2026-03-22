package com.redditApp.events;

import com.redditApp.BaseEvent;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent extends BaseEvent {

    private Long orderId;
    private Long paymentId;
    private Long userId;
    private String reason;

    private Instant failedAt;
}