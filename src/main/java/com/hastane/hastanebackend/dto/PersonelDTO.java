package com.hastane.hastanebackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Yeni bir personel oluşturmak için frontend'den gelen verileri taşıyan nesne (Data Transfer Object).
 */
@Getter
@Setter
public class PersonelDTO {

    // Personel Bilgileri
    private String ad;
    private String soyad;
    private String telefon;

    // İlişkili Departman'ın ID'si
    private Integer departmanId;

    // Kullanıcı Bilgileri
    private String email;
    private String sifre;

    // Kullanıcıya atanacak rollerin adları (Örn: "ROLE_DOKTOR", "ROLE_YONETICI")
    private Set<String> roller;
}