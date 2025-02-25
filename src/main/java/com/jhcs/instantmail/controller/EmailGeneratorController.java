package com.jhcs.instantmail.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jhcs.instantmail.dto.EmailRequest;
import com.jhcs.instantmail.service.EmailGeneratorService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controlador REST responsável por lidar com requisições relacionadas à geração de respostas de e-mail.
 * Mapeia as requisições para o endpoint /api/email.
 */
@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
public class EmailGeneratorController {

    private final EmailGeneratorService emailGeneratorService;

    /**
     * Endpoint para gerar uma resposta de e-mail.
     * Recebe um objeto EmailRequest no corpo da requisição e retorna a resposta gerada.
     *
     * @param emailRequest Objeto contendo o conteúdo do e-mail original e o tom desejado para a resposta.
     * @return ResponseEntity contendo a resposta gerada pela API externa.
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest) {
        String response = emailGeneratorService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(response);
    }
}