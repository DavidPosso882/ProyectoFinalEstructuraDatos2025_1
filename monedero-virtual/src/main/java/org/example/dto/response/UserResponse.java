package org.example.dto.response;

import lombok.Data;
import org.example.model.UserRank;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserRank rank;
    private LocalDateTime createdAt;
    private List<WalletResponse> wallets;
    private PointsAccountResponse pointsAccount;
    private int unreadNotificationsCount;
}
