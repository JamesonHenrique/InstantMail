package com.jhcs.instantmail.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhcs.instantmail.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailGeneratorService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClient, ObjectMapper objectMapper) {
        this.webClient = webClient.build();
        this.objectMapper = objectMapper;
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        log.info("Generating email reply with tone: {}",
                Optional.ofNullable(emailRequest.getTone()).orElse("default"));

        validateRequest(emailRequest);
        String prompt = buildPrompt(emailRequest);
        Map<String, Object> requestBody = createRequestBody(prompt);

        try {
            String response = callExternalApi(requestBody);
            log.debug("Received response from Gemini API");
            return extractResponseContent(response);
        } catch (Exception e) {
            log.error("Error generating email reply", e);
            throw new RuntimeException("Failed to generate email reply", e);
        }
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

    private String callExternalApi(Map<String, Object> requestBody) {
        log.debug("Calling Gemini API");
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
            log.error("Error extracting content from API response", e);
            throw new RuntimeException("Error processing API response", e);
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        String senderName = extractSenderName(emailRequest.getEmailContent());

        return "INSTRUÇÕES DETALHADAS PARA GERAÇÃO DE RESPOSTA DE E-MAIL:\n\n" +
                "===== TAREFA PRINCIPAL =====\n" +
                "Gere uma resposta profissional em português para o e-mail abaixo, seguindo RIGOROSAMENTE todas as instruções.\n\n" +

                "===== CONTEÚDO =====\n" +
                "- A resposta deve ser concisa e objetiva\n" +
                "- Abordar todos os pontos principais da mensagem original\n" +
                "- Evitar informações irrelevantes ou redundantes\n" +
                "- Tom da resposta: " + Optional.ofNullable(emailRequest.getTone()).filter(t -> !t.trim().isEmpty()).orElse("profissional") + "\n\n" +

                "===== FORMATAÇÃO OBRIGATÓRIA =====\n" +
                "- Começar com uma saudação apropriada (ex: 'Olá,' ou 'Prezado(a),'), sem linha de assunto\n" +
                "- Usar parágrafos curtos e bem estruturados\n" +
                "- Usar quebras de linha apropriadas para facilitar a leitura\n\n" +

                "===== INSTRUÇÕES CRÍTICAS SOBRE ASSINATURA - SIGA EXATAMENTE =====\n" +
                (senderName != null ?
                        "✓ INCLUIR ASSINATURA NATURAL: Finalize o email com uma despedida adequada ao tom (" + emailRequest.getTone() + ") seguida do nome do remetente.\n" +
                                "- Exemplos: 'Abraços, [Nome]', 'Atenciosamente, [Nome]', 'Até breve, [Nome]'\n" +
                                "- Use uma despedida que combine com o tom da mensagem\n" +
                                "- O nome DEVE ser exatamente: " + senderName + "\n\n" :
                        "✓ NÃO INCLUIR ASSINATURA: Não adicione NENHUMA conclusão formal como 'Atenciosamente,' ou qualquer tipo de assinatura.\n" +
                                "- PROIBIDO adicionar 'Atenciosamente,' ou frases similares\n" +
                                "- PROIBIDO adicionar '[Your Name]', '[Nome]' ou qualquer placeholder\n" +
                                "- TERMINE o email após o último parágrafo do conteúdo principal\n\n") +

                "===== EXEMPLOS DE FINALIZAÇÃO CORRETA =====\n" +
                (senderName != null ?
                        "CORRETO ✓\n...final do conteúdo do email.\n\nAbraços,\n" + senderName + "\n\n" +
                                "CORRETO ✓\n...final do conteúdo do email.\n\nAtenciosamente,\n" + senderName + "\n\n" +
                                "INCORRETO ✗\n...final do conteúdo do email.\n\nAtenciosamente,\n[Your Name]\n\n" :
                        "CORRETO ✓\n...final do conteúdo do email.\n\n" +
                                "INCORRETO ✗\n...final do conteúdo do email.\n\nAtenciosamente,\n[Your Name]\n\n") +

                "===== AVISO IMPORTANTE =====\n" +
                "As instruções sobre assinatura são ABSOLUTAMENTE OBRIGATÓRIAS e devem ser seguidas com precisão.\n" +
                "Se o email original tiver uma assinatura ou indicar quem enviou, a resposta DEVE incluir assinatura com o mesmo nome.\n\n" +

                "===== E-MAIL ORIGINAL PARA RESPONDER =====\n" +
                emailRequest.getEmailContent();
    }

    private String extractSenderName(String emailContent) {
        Pattern signaturePattern = Pattern.compile(
                "(?i)(Abraços|Atenciosamente|Obrigado|Att|Att\\.|Att,\\s|Cordialmente|Saudações|Regards|Best regards|Cheers),?\\s*\\n?\\s*([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)\\s*$");

        Matcher matcher = signaturePattern.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group(2);
        }

        Pattern fromPattern = Pattern.compile("(?i)(?:De|From|Enviado por|Sent by):?\\s*(.*?)\\s*<.*?>|(.*?)\\s*<");
        matcher = fromPattern.matcher(emailContent);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }

        return null;
    }
}