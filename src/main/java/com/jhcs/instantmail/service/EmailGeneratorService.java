package com.jhcs.instantmail.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhcs.instantmail.dto.EmailRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Serviço responsável por gerar respostas de e-mail profissionais.
 * Utiliza um cliente WebClient para fazer requisições a uma API externa.
 */
@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    /**
     * Construtor da classe EmailGeneratorService.
     *
     * @param webClient Builder do WebClient utilizado para construir o cliente HTTP.
     */
    public EmailGeneratorService(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    /**
     * Gera uma resposta de e-mail com base no conteúdo do e-mail original.
     *
     * @param emailRequest Objeto contendo o conteúdo do e-mail original e o tom desejado para a resposta.
     * @return Resposta gerada pela API externa.
     */
    public String generateEmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
                                                );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractResponseContent(response);
    }

    /**
     * Extrai o conteúdo da resposta da API externa.
     *
     * @param response Resposta JSON da API externa.
     * @return Conteúdo da resposta extraído.
     */
    private String extractResponseContent(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            return "Error processing request: " + e.getMessage();
        }
    }

    /**
     * Constrói o prompt a ser enviado para a API externa com base no conteúdo do e-mail original.
     *
     * @param emailRequest Objeto contendo o conteúdo do e-mail original e o tom desejado para a resposta.
     * @return Prompt construído.
     */
    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line ");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\nOriginal email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}