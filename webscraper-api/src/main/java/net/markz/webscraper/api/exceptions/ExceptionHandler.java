package net.markz.webscraper.api.exceptions;

import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<ErrorResponse> handle(final RuntimeException e) {
        log.error(e.toString());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse().message("Unexpected error, please contact the developer at zdy120939259@outlook.com"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<ErrorResponse> handle(final WebscraperException e) {
        log.error(e.toString());
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(new ErrorResponse().message(e.getMessage()));
    }
}
