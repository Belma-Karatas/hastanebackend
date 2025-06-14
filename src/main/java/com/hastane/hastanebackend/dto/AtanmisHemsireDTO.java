package com.hastane.hastanebackend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AtanmisHemsireDTO {
    private Integer yatisHemsireAtamaId; // YatisHemsireAtama entity'sinin ID'si (atamayı silmek için gerekebilir)
    private Integer hemsirePersonelId;
    private String hemsireAdiSoyadi;
    private LocalDateTime atamaTarihi;
    // private String hemsireBransAdi; // Eğer hemşirelerin de branşları varsa eklenebilir
}