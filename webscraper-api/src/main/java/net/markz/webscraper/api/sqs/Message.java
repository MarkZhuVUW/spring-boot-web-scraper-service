package net.markz.webscraper.api.sqs;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
public class Message<T> {
    private String eventType;
    private LocalDateTime lastModified;
    private T data;
}
