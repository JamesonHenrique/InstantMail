package com.jhcs.instantmail.dto;

import lombok.Data;


/**
 * Classe DTO (Data Transfer Object) que representa uma requisição de e-mail.
 * Contém o conteúdo do e-mail original e o tom desejado para a resposta.
 */
@Data
public class EmailRequest {

    /**
     * Conteúdo do e-mail original.
     */
    private String emailContent;

    /**
     * Tom desejado para a resposta do e-mail.
     * Pode ser, por exemplo, formal, informal, amigável, etc.
     */
    private String tone;
}
