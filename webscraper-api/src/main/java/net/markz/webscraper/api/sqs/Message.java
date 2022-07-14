package net.markz.webscraper.api.sqs;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class Message<T> {
    private String eventType;
    private long timestamp;
    private List<T> data;
}
