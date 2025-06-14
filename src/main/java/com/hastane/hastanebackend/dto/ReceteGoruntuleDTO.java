package com.hastane.hastanebackend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder // Bu DTO'yu servis katmanında kolayca oluşturmak için
public class ReceteGoruntuleDTO {

    private Integer id; // Recete ID
    private LocalDate receteTarihi;
    private LocalDateTime olusturulmaZamani;

    // İlişkili Muayene Bilgileri
    private Integer muayeneId;
    // private LocalDateTime muayeneTarihi; // Gerekirse eklenebilir

    // İlişkili Hasta Bilgileri (Muayene üzerinden)
    private Integer hastaId;
    private String hastaAdiSoyadi;
    // private String hastaTcKimlikNo; // Gerekirse eklenebilir

    // İlişkili Doktor Bilgileri (Muayene üzerinden)
    private Integer doktorId; // Personel ID
    private String doktorAdiSoyadi;
    private String doktorBransAdi; // Gerekirse eklenebilir

    private List<ReceteIlacDetayDTO> ilaclar; // Reçetedeki ilaçların detayları
}