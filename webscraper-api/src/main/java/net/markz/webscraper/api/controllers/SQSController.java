package net.markz.webscraper.api.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.SqsApiDelegate;
import net.markz.webscraper.api.services.SQSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SQSController implements SqsApiDelegate {
    private final SQSService sqsService;

    @Override
    public final ResponseEntity<Void> sqsProduce() {
        sqsService.sqsProduce();
        return null;
    }

    @Override
    public final ResponseEntity<Void> sqsPoll() {
        sqsService.sqsLongPoll();
        return null;
    }
}
