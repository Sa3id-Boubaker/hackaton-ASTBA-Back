package tn.esprit.astba.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String newPassword;
    private String confirmPassword;
}