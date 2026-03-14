package com.redditApp;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {

    private String eventId = UUID.randomUUID().toString();
    private Instant timestamp = Instant.now();
}
