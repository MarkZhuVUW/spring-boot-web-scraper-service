package net.markz.webscraper.api.consumers;

import lombok.Data;
import lombok.ToString;

import java.util.Calendar;

@Data
@ToString
public class Message<T> {
    private String eventType;
    private String operation;
    private Calendar lastModified;
    private T data;
}
