package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; // BU SATIRI EKLE
import lombok.AllArgsConstructor; // BU SATIRI EKLE (Opsiyonel ama genellikle Builder ile iyi gider)
import lombok.Setter; // BU SATIRI EKLE (veya aktif et)

import java.time.LocalDateTime;

@Getter
@Setter // SETTER'LARI EKLE/AKTİF ET
@Builder
@NoArgsConstructor // BOŞ CONSTRUCTOR EKLE
@AllArgsConstructor // TÜM ALANLARI ALAN CONSTRUCTOR EKLE (Builder ile iyi çalışır)
public class MuayeneOlusturDTO {

    private Integer randevuId;

    @NotNull(message = "Hasta ID boş olamaz.")
    private Integer hastaId;

    @NotNull(message = "Muayene tarihi ve saati boş olamaz.")
    private LocalDateTime muayeneTarihiSaati;

    private String hikaye;
    
    @NotNull(message = "Tanı boş olamaz.")
    private String tani;

    private String tedaviNotlari;
}