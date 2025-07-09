package com.jhcs.instantmail.controller;

import com.jhcs.instantmail.dto.EmailRequest;
import com.jhcs.instantmail.service.EmailGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
public class EmailGeneratorController {

    private final EmailGeneratorService emailGeneratorService;

    @PostMapping("/generate")
    @CrossOrigin(origins = {"https://mail.google.com", "https://www.instantmail.shop", "chrome-extension://*"})
    public ResponseEntity<Mono<String>> generateEmail(@RequestBody EmailRequest emailRequest,
                                                      @RequestHeader(value = "Origin", required = false) String origin) {

        if (origin != null && !isAllowedOrigin(origin)) {
            return ResponseEntity.status(403).build();
        }

        Mono<String> response = emailGeneratorService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(response);
    }

    private boolean isAllowedOrigin(String origin) {
        return origin.matches("https://mail\\.google\\.com") ||
                origin.matches("https://www\\.instantmail\\.shop") ||
                origin.startsWith("chrome-extension://");
    }
}