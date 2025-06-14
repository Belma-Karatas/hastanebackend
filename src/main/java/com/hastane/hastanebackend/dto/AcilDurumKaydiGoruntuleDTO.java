package com.hastane.hastanebackend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AcilDurumKaydiGoruntuleDTO {

    private Integer id;
    private String aciklama;
    private String konum;
    private LocalDateTime olayZamani;
    private String durum;

    // Tetikleyen Personel Bilgileri
    private Integer tetikleyenPersonelId;
    private String tetikleyenPersonelAdiSoyadi;
    // private String tetikleyenPersonelRolu; // Gerekirse

    // Hasta Bilgileri (Eğer varsa)
    private Integer hastaId;
    private String hastaAdiSoyadi;
    // private String hastaOdaNo; // Gerekirse
}