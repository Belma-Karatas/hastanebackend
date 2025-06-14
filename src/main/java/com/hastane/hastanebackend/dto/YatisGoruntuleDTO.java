package com.hastane.hastanebackend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import java.util.List;

@Getter
@Setter
@Builder
public class YatisGoruntuleDTO {

    private Integer id; // Yatis ID

    // Hasta Bilgileri
    private Integer hastaId;
    private String hastaAdiSoyadi;
    private String hastaTcKimlikNo; // Gerekirse

    // Yatak Bilgileri
    private Integer yatakId;
    private String yatakNumarasi;
    private String odaNumarasi;
    private String katAdi;

    // Sorumlu Doktor Bilgileri
    private Integer sorumluDoktorId; // Personel ID
    private String sorumluDoktorAdiSoyadi;
    private String sorumluDoktorBransAdi; // Gerekirse

    private LocalDateTime girisTarihi;
    private LocalDateTime cikisTarihi; // Null olabilir
    private String yatisNedeni;

   
     private List<AtanmisHemsireDTO> hemsireler;
}
