package net.markz.webscraper.api.sqs;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Calendar;

@Data
@ToString
@Builder
public class Message<T> {
    private String eventType;
    private Calendar lastModified;
    private T data;
}
