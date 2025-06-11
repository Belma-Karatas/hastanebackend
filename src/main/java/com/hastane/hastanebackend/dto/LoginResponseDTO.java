package com.hastane.hastanebackend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
    // İstersen kullanıcı bilgilerini de ekleyebilirsin (email, roller vb.)
    // private String email;
    // private java.util.List<String> roller;
}