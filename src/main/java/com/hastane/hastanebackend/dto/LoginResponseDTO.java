package com.hastane.hastanebackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor; // Boş constructor için eklendi

import java.util.List; // Roller için

@Data
@NoArgsConstructor // Lombok'un tüm alanları içeren constructor'ı otomatik oluşturması için boş constructor da gerekebilir.
                 // Veya @AllArgsConstructor yerine sadece gerekli constructor'ları manuel yazabilirsiniz.
public class LoginResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
    private List<String> roller; // Backend'den gelen roller (örn: ["ROLE_HASTA", "ROLE_ADMIN"])
    private Integer hastaId;     // YENİ EKLENDİ: Eğer kullanıcı HASTA ise bu ID dolu olacak, değilse null olabilir.
    private String email;        // Opsiyonel: Kullanıcının email'ini de döndürebiliriz.
    private Integer kullaniciId; // Opsiyonel: Kullanıcının genel Kullanici ID'sini de döndürebiliriz.

    // Sadece accessToken ve tokenType ile constructor (JWT üretimi için kullanılabilir)
    public LoginResponseDTO(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    // Tüm alanları içeren constructor (opsiyonel, Lombok @AllArgsConstructor da kullanılabilir ama
    // bazı alanlar null olabileceği için bu daha kontrolü olabilir)
    public LoginResponseDTO(String accessToken, String tokenType, List<String> roller, Integer hastaId, String email, Integer kullaniciId) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.roller = roller;
        this.hastaId = hastaId;
        this.email = email;
        this.kullaniciId = kullaniciId;
    }

    // Lombok @Data zaten getter/setter'ları oluşturur, ancak isterseniz manuel de eklenebilir:
    // public List<String> getRoller() { return roller; }
    // public void setRoller(List<String> roller) { this.roller = roller; }
    // public Integer getHastaId() { return hastaId; }
    // public void setHastaId(Integer hastaId) { this.hastaId = hastaId; }
    // public String getEmail() { return email; }
    // public void setEmail(String email) { this.email = email; }
    // public Integer getKullaniciId() { return kullaniciId; }
    // public void setKullaniciId(Integer kullaniciId) { this.kullaniciId = kullaniciId; }
}