package net.markz.webscraper.api.sqs;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Message<T> {
    private String eventType;
    private long timestamp;
    private List<T> data;

    @JsonCreator
    public Message() {
        // For deserializing the message.
    }
}
