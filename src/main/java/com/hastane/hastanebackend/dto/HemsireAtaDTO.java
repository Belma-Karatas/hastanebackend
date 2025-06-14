package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HemsireAtaDTO {

    @NotNull(message = "Hemşire Personel ID boş olamaz.")
    @Positive(message = "Hemşire Personel ID pozitif bir sayı olmalıdır.")
    private Integer hemsirePersonelId;
}