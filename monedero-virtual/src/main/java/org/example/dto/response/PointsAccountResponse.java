package org.example.dto.response;

import lombok.Data;
import org.example.model.UserRank;

import java.time.LocalDateTime;

@Data
public class PointsAccountResponse {
    private Long id;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer redeemedPoints;
    private UserRank currentRank;
    private Integer pointsToNextRank;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
