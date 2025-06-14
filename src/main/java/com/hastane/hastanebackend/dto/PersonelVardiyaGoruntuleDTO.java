package com.hastane.hastanebackend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class PersonelVardiyaGoruntuleDTO {

    private Integer id; // PersonelVardiya kaydının ID'si

    // Personel Bilgileri
    private Integer personelId;
    private String personelAdiSoyadi;
    // private String personelRolu; // Gerekirse eklenebilir

    // Vardiya Bilgileri
    private Integer vardiyaId;
    private String vardiyaAdi;
    private LocalTime vardiyaBaslangicSaati;
    private LocalTime vardiyaBitisSaati;

    private LocalDate tarih; // Vardiyanın olduğu gün
    private LocalDateTime atamaTarihi; // Bu kaydın oluşturulma zamanı
}