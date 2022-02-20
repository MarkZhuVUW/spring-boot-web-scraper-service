package net.markz.webscraper.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WebscraperException extends RuntimeException {
  private final HttpStatus httpStatus;

  public WebscraperException(final HttpStatus httpStatus, final String message) {
    super(message);
    this.httpStatus = httpStatus;
  }
}
