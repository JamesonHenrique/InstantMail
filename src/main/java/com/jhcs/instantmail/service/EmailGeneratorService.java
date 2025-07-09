package com.jhcs.instantmail.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhcs.instantmail.dto.EmailRequest;
import com.jhcs.instantmail.utils.EmailParticipants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public String generateEmailReply(EmailRequest emailRequest) {
        log.info("Gerando resposta de e-mail com tom: {}",
                Optional.ofNullable(emailRequest.getTone()).orElse("padrão"));

        validateRequest(emailRequest);
        String prompt = buildPrompt(emailRequest);
        Map<String, Object> requestBody = createRequestBody(prompt);

        try {
            String response = callExternalApi(requestBody);
            log.debug("Resposta recebida da API Gemini");
            return extractResponseContent(response);
        } catch (Exception e) {
            log.error("Erro ao gerar resposta de e-mail", e);
            throw new RuntimeException("Falha ao gerar resposta de e-mail", e);
        }
    }

    private void validateRequest(EmailRequest emailRequest) {
        if (emailRequest == null || emailRequest.getEmailContent() == null ||
                emailRequest.getEmailContent().trim().isEmpty()) {
            log.warn("Requisição de e-mail inválida recebida");
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

    private String callExternalApi(Map<String, Object> requestBody) {
        log.debug("Chamando API Gemini");
        return webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
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
            log.error("Erro ao extrair conteúdo da resposta da API", e);
            throw new RuntimeException("Erro ao processar resposta da API", e);
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        Objects.requireNonNull(emailRequest, "EmailRequest não pode ser nulo");

        EmailParticipants participants = extractService.extractParticipants(emailRequest.getEmailContent());
        String senderName = extractFirstName(participants.getSender());
        String recipientName = extractFirstName(participants.getRecipient());
        String tone = Optional.ofNullable(emailRequest.getTone())
                .filter(t -> !t.trim().isEmpty())
                .orElse("profissional");

        return """
           INSTRUÇÕES DETALHADAS PARA GERAÇÃO DE RESPOSTA DE E-MAIL:

           ===== TAREFA PRINCIPAL =====
           Gere uma resposta profissional em português para o e-mail abaixo, seguindo RIGOROSAMENTE todas as instruções.

           ===== INFORMAÇÕES DOS PARTICIPANTES =====
           - REMETENTE ORIGINAL: %s
           - DESTINATÁRIO: %s
           - TOM SOLICITADO: %s

           ===== DIRETRIZES DE RESPOSTA =====
           1. SAUDAÇÃO: "Olá %s,"
           2. CONTEÚDO:
              - Aborde todos os pontos principais do e-mail original
              - Utilize parágrafos curtos e objetivos
              - Mantenha o tom %s
           3. FORMATAÇÃO:
              - Máximo 4-5 linhas por parágrafo
              - Espaçamento entre parágrafos
           4. ENCERRAMENTO:
              "Abraços,
              %s"

           ===== E-MAIL ORIGINAL PARA REFERÊNCIA =====
           %s
           """.formatted(
                participants.getSender(),
                participants.getRecipient(),
                tone,
                senderName,
                tone,
                recipientName,
                emailRequest.getEmailContent()
        );
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Nome";
        }
        return fullName.split("\\s+")[0];
    }
}