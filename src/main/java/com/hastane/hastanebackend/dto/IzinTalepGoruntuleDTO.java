package com.hastane.hastanebackend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class IzinTalepGoruntuleDTO {

    private Integer id;

    
    private Integer talepEdenPersonelId;
    private String talepEdenPersonelAdiSoyadi;
    // private String talepEdenPersonelDepartmanAdi; // Gerekirse

    
    private Integer izinTuruId;
    private String izinTuruAdi;

    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
    private Integer gunSayisi;
    private String aciklama;
    private LocalDateTime talepTarihi;
    private String durum;

    // Onaylayan Yönetici Bilgileri (Eğer onaylandı/reddedildiyse)
    private Integer onaylayanYoneticiId; // Personel ID
    private String onaylayanYoneticiAdiSoyadi;
    private LocalDateTime onayTarihi;
}