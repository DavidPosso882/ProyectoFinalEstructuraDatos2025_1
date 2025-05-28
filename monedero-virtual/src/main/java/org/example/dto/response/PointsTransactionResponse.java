package org.example.dto.response;

import lombok.Data;
import org.example.model.PointsTransactionType;

import java.time.LocalDateTime;

@Data
public class PointsTransactionResponse {
    private Long id;
    private Integer amount;
    private PointsTransactionType type;
    private String description;
    private LocalDateTime transactionDate;
    private Long relatedTransactionId;
}
