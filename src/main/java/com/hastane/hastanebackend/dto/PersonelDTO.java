package com.hastane.hastanebackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Yeni bir personel oluşturmak veya güncellemek için frontend'den gelen verileri taşıyan nesne (Data Transfer Object).
 */
@Getter
@Setter
public class PersonelDTO {

    // Personel Bilgileri
    private String ad;
    private String soyad;
    private String telefon; // Opsiyonel olabilir

    // İlişkili Departman'ın ID'si
    private Integer departmanId; // Opsiyonel olabilir

    // Kullanıcı Bilgileri
    private String email;
    private String sifre; // Yeni personel oluştururken veya şifre güncellenirken kullanılır

    // Kullanıcıya atanacak rollerin adları (Örn: "ROLE_DOKTOR", "ROLE_ADMIN")
    private Set<String> roller;

    // Doktor ise, atanacak Branş'ın ID'si
    private Integer bransId; // YENİ EKLENDİ - Bu alan sadece doktorlar için anlamlı olacak
}