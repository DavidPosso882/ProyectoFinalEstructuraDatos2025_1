package org.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsRedemptionRequest {
    @NotNull
    @Min(1)
    private Integer points;

    @NotBlank
    private String benefitCode;
}
