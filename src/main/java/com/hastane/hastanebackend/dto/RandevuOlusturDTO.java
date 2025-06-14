package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.Future; // Gelecek bir tarih olmalı
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RandevuOlusturDTO {

    @NotNull(message = "Hasta ID boş olamaz.")
    private Integer hastaId;

    @NotNull(message = "Doktor ID boş olamaz.")
    private Integer doktorId; // Bu, Personel ID'sine karşılık gelecek

    @NotNull(message = "Randevu tarihi ve saati boş olamaz.")
    @Future(message = "Randevu tarihi ve saati gelecek bir zamanda olmalıdır.") // Temel bir validasyon
    private LocalDateTime randevuTarihiSaati;

    // Açıklama alanını Randevu entity'sinden kaldırdığımız için DTO'dan da kaldırıyoruz.
    // private String aciklama;
}