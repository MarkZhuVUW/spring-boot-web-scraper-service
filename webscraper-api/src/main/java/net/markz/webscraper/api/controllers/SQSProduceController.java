package net.markz.webscraper.api.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.markz.webscraper.api.SqsProduceApiDelegate;
import net.markz.webscraper.api.services.SQSProduceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class SQSProduceController implements SqsProduceApiDelegate {
    private final SQSProduceService sqsProduceService;

    @Override
    public final ResponseEntity<Void> sqsProduce() {
        sqsProduceService.sqsProduce();
        return null;
    }
}
