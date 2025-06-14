package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VardiyaDTO {

    private Integer id;

    @NotBlank(message = "Vardiya adı boş olamaz.")
    @Size(max = 100, message = "Vardiya adı en fazla 100 karakter olabilir.")
    private String ad;

    @NotNull(message = "Başlangıç saati boş olamaz.")
    private LocalTime baslangicSaati;

    @NotNull(message = "Bitiş saati boş olamaz.")
    private LocalTime bitisSaati;
}