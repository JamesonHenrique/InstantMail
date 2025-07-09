package com.jhcs.instantmail.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhcs.instantmail.dto.EmailRequest;
import com.jhcs.instantmail.utils.EmailParticipants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class EmailGeneratorService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ExtractService extractService;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClient, ObjectMapper objectMapper, ExtractService extractService) {
        this.webClient = webClient.build();
        this.objectMapper = objectMapper;
        this.extractService = extractService;
    }
    @Cacheable(value = "emailReplies", key = "#emailRequest.hashCode()")
    public Mono<String> generateEmailReply(EmailRequest emailRequest) {
        log.info("Generating email reply with tone: {}",
                Optional.ofNullable(emailRequest.getTone()).orElse("default"));

        validateRequest(emailRequest);
        String prompt = buildPrompt(emailRequest);
        Map<String, Object> requestBody = createRequestBody(prompt);

        return webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractResponseContent)
                .doOnError(e -> log.error("Error generating email reply", e))
                .onErrorMap(e -> new RuntimeException("Failed to generate email reply", e));
    }

    private void validateRequest(EmailRequest emailRequest) {
        if (emailRequest == null || emailRequest.getEmailContent() == null ||
                emailRequest.getEmailContent().trim().isEmpty()) {
            log.warn("Invalid email request received");
            throw new IllegalArgumentException("O conteúdo do e-mail não pode ser nulo ou vazio");
        }
    }

    private Map<String, Object> createRequestBody(String prompt) {
        return Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );
    }

    private String extractResponseContent(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            log.error("Error extracting content from API response", e);
            throw new RuntimeException("Error processing API response", e);
        }
    }


    private String buildPrompt(EmailRequest emailRequest) {
        EmailParticipants participants = extractService.extractParticipants(emailRequest.getEmailContent());
        return "INSTRUÇÕES DETALHADAS PARA GERAÇÃO DE RESPOSTA DE E-MAIL:\n\n" +
                "===== TAREFA PRINCIPAL =====\n" +
                "Gere uma resposta profissional em português para o e-mail abaixo, seguindo RIGOROSAMENTE todas as instruções.\n\n" +

                "===== INFORMAÇÕES DOS PARTICIPANTES =====\n" +
                "- REMETENTE ORIGINAL (quem enviou este email): " + participants.getSender() + "\n" +
                "- DESTINATÁRIO (quem recebeu/está respondendo): " + participants.getRecipient() + "\n\n" +

                "===== CONTEÚDO =====\n" +
                "- Saudação inicial deve ser direcionada ao remetente original: 'Olá " + participants.getSender().split(" ")[0] + ",'\n" +
                "- A resposta deve abordar todos os pontos principais\n" +
                "- Tom: " + Optional.ofNullable(emailRequest.getTone()).filter(t -> !t.trim().isEmpty()).orElse("profissional") + "\n\n" +

                "===== FORMATAÇÃO OBRIGATÓRIA =====\n" +
                "- Saudação: 'Olá " + participants.getSender().split(" ")[0] + ",'\n" +
                "- Parágrafos curtos e bem estruturados\n" +
                "- Quebras de linha adequadas\n\n" +

                "===== ASSINATURA =====\n" +
                "- Finalizar com:\n" +
                "Abraços,\n" +
                participants.getRecipient().split(" ")[0] + "\n\n" +

                "===== E-MAIL ORIGINAL =====\n" +
                emailRequest.getEmailContent();
    }
}