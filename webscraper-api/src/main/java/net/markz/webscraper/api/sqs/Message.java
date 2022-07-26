package net.markz.webscraper.api.sqs;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Data
@ToString
public class Message<T> {
    private String eventType;
    private long timestamp;
    private List<T> data;
    private UUID msgId;

    @JsonCreator
    public Message() {
        // For deserializing the message.
    }
}
