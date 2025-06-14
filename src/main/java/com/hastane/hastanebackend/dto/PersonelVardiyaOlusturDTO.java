package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PersonelVardiyaOlusturDTO {

    @NotNull(message = "Personel ID boş olamaz.")
    @Positive(message = "Personel ID pozitif bir sayı olmalıdır.")
    private Integer personelId;

    @NotNull(message = "Vardiya ID boş olamaz.")
    @Positive(message = "Vardiya ID pozitif bir sayı olmalıdır.")
    private Integer vardiyaId;

    @NotNull(message = "Tarih boş olamaz.")
    private LocalDate tarih; // Atamanın yapılacağı gün
}