package com.jhcs.instantmail.service;

import com.jhcs.instantmail.utils.EmailParticipants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ExtractService {
    private static final Pattern RECIPIENT_PATTERN = Pattern.compile("(?i)(Oi|Olá|Oi,|Olá,)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)");
    private static final Pattern SENDER_PATTERN = Pattern.compile("(?i)(Abraços|Atenciosamente|Obrigado),?\\s*\\n?\\s*([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)\\s*$");

    public EmailParticipants extractParticipants(String emailContent) {
        String recipient = extractRecipient(emailContent);

        String sender = extractSender(emailContent);

        return new EmailParticipants(sender, recipient);
    }

    public String extractRecipient(String emailContent) {
        Matcher matcher = RECIPIENT_PATTERN.matcher(emailContent);
        return matcher.find() ? matcher.group(2) : "Nome";
    }

    public String extractSender(String emailContent) {
        Matcher matcher = SENDER_PATTERN.matcher(emailContent);
        return matcher.find() ? matcher.group(2) : "Remetente";
    }
}
