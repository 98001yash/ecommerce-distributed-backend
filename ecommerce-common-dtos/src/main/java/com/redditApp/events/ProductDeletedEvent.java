package com.redditApp.events;

import com.redditApp.BaseEvent;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDeletedEvent extends BaseEvent {

    private Long productId;
    private Long sellerId;
}

