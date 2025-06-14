package com.hastane.hastanebackend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder // Bu DTO'yu servis katmanında kolayca oluşturmak için
public class SevkGoruntuleDTO {

    private Integer id; // Sevk kaydının ID'si

    // Hasta Bilgileri
    private Integer hastaId;
    private String hastaAdiSoyadi;
    // private String hastaTcKimlikNo; // Gerekirse eklenebilir

    // Sevk Eden Doktor Bilgileri
    private Integer sevkEdenDoktorId; // Personel ID
    private String sevkEdenDoktorAdiSoyadi;
    // private String sevkEdenDoktorBransAdi; // Gerekirse

    private LocalDateTime sevkTarihi; // Sevk kaydının oluşturulduğu tarih veya planlanan sevk tarihi
    private String hedefKurum;
    private String hedefServis;
    private String sevkNedeni;
    private String durum; // PLANLANDI, TAMAMLANDI, IPTAL
}