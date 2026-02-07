package tn.esprit.astba.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponse {
    private String message;
    private String token;
    private UserResponse user;
    private Boolean mustChangePassword; // ✅ NOUVEAU
}