package com.hastane.hastanebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // Tüm alanlar için constructor'ı da ekleyebiliriz

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor // Tüm alanları içeren constructor'ı Lombok'un oluşturması için eklendi
public class LoginResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
    private List<String> roller;
    private String email;        // Giriş yapılan email (JWT'nin subject'i)
    private Integer kullaniciId; // Kullanici entity'sinin ID'si
    private Integer personelId;  // Personel entity'sinin ID'si (eğer kullanıcı personel ise, değilse null)
    private Integer hastaId;     // Hasta entity'sinin ID'si (eğer kullanıcı hasta ise, değilse null)

    // Sadece token ve type için constructor (Lombok @AllArgsConstructor varken bu gereksiz olabilir,
    // ama spesifik bir kullanım durumu varsa kalabilir)
    public LoginResponseDTO(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    // Lombok @AllArgsConstructor zaten tüm alanları içeren bir constructor oluşturacaktır.
    // Eğer spesifik bir sıralama veya mantık yoksa bu manuel constructor'a gerek kalmaz.
    // public LoginResponseDTO(String accessToken, String tokenType, List<String> roller, String email, Integer kullaniciId, Integer personelId, Integer hastaId) {
    //     this.accessToken = accessToken;
    //     this.tokenType = tokenType;
    //     this.roller = roller;
    //     this.email = email;
    //     this.kullaniciId = kullaniciId;
    //     this.personelId = personelId;
    //     this.hastaId = hastaId;
    // }
}