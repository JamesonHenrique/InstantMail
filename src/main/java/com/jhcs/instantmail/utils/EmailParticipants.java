package com.jhcs.instantmail.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailParticipants {
    private String sender;
    private String recipient;
}