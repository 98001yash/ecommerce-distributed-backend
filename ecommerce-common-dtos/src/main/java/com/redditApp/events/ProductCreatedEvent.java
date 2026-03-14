package com.redditApp.events;

import com.redditApp.BaseEvent;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreatedEvent extends BaseEvent {

    private Long productId;
    private String name;
    private BigDecimal price;
    private Long sellerId;
    private String category;
}
