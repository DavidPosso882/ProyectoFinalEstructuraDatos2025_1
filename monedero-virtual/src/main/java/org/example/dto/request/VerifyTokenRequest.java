package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyTokenRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String token;
}
