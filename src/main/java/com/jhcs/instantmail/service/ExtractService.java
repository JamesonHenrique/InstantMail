package com.jhcs.instantmail.service;

import com.jhcs.instantmail.utils.EmailParticipants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ExtractService {
    private static final Pattern RECIPIENT_PATTERN = Pattern.compile(
            "(?i)(?:Oi|Olá|Prezado|Caro|Cara)[,\\s]+([A-ZÀ-Ú][a-zà-ú]+(?:\\s+[A-ZÀ-Ú][a-zà-ú]+)*)"
    );

    private static final Pattern SENDER_PATTERN = Pattern.compile(
            "(?i)(?:Abraços|Atenciosamente|Obrigado|Saudações|Cordialmente)[,\\s]*\\n?\\s*([A-ZÀ-Ú][a-zà-ú]+(?:\\s+[A-ZÀ-Ú][a-zà-ú]+)*)\\s*$"
    );

    public EmailParticipants extractParticipants(String emailContent) {
        if (emailContent == null || emailContent.isBlank()) {
            return new EmailParticipants("Remetente", "Destinatário");
        }

        return new EmailParticipants(
                extractSender(emailContent),
                extractRecipient(emailContent)
        );
    }

    public String extractRecipient(String emailContent) {
        Matcher matcher = RECIPIENT_PATTERN.matcher(emailContent);
        return matcher.find() ? matcher.group(1).trim() : "Destinatário";
    }

    public String extractSender(String emailContent) {
        Matcher matcher = SENDER_PATTERN.matcher(emailContent);
        return matcher.find() ? matcher.group(1).trim() : "Remetente";
    }
}
