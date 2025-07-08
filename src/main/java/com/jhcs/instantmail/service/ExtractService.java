package com.jhcs.instantmail.service;

import com.jhcs.instantmail.utils.EmailParticipants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class ExtractService {

    public EmailParticipants extractParticipants(String emailContent) {
        String recipient = extractRecipient(emailContent);

        String sender = extractSender(emailContent);

        return new EmailParticipants(sender, recipient);
    }

    public String extractRecipient(String emailContent) {
        Pattern pattern = Pattern.compile("(?i)(Oi|Olá|Oi,|Olá,)\\s+([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)");
        Matcher matcher = pattern.matcher(emailContent);
        return matcher.find() ? matcher.group(2) : "Nome";
    }

    public String extractSender(String emailContent) {
        Pattern pattern = Pattern.compile("(?i)(Abraços|Atenciosamente|Obrigado),?\\s*\\n?\\s*([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)\\s*$");
        Matcher matcher = pattern.matcher(emailContent);
        return matcher.find() ? matcher.group(2) : "Remetente";
    }
}
