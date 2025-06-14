package com.hastane.hastanebackend.dto;

import lombok.Builder; // EKLENDİ
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder // EKLENDİ
public class MuayeneGoruntuleDTO {
    private Integer id;
    private Integer randevuId; // İlişkili randevunun ID'si (varsa)
    private LocalDateTime muayeneTarihiSaati;
    private String hikaye;
    private String tani;
    private String tedaviNotlari;
    private LocalDateTime olusturulmaTarihi;

    private Integer hastaId;
    private String hastaAdiSoyadi;

    private Integer doktorId;
    private String doktorAdiSoyadi;
    private String doktorBransAdi; // Doktorun branşını da göstermek faydalı olabilir
}