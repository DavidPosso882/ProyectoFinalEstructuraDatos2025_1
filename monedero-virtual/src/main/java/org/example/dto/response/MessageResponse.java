package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String message;
    private String maskedEmail;

    public MessageResponse(String message) {
        this.message = message;
    }
}
