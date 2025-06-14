package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SevkOlusturDTO {

    @NotNull(message = "Hasta ID boş olamaz.")
    @Positive(message = "Hasta ID pozitif bir sayı olmalıdır.")
    private Integer hastaId;

    // sevkEdenDoktorId, işlemi yapan doktordan (JWT token) alınacak, DTO'da olmasına gerek yok.

    @NotNull(message = "Sevk tarihi boş olamaz.")
    @FutureOrPresent(message = "Sevk tarihi geçmiş bir tarih olamaz.") // Genellikle sevk ileriye dönük planlanır
    private LocalDateTime sevkTarihi;

    @NotBlank(message = "Hedef kurum boş olamaz.")
    @Size(max = 150, message = "Hedef kurum en fazla 150 karakter olabilir.")
    private String hedefKurum;

    @Size(max = 100, message = "Hedef servis en fazla 100 karakter olabilir.")
    private String hedefServis; // Opsiyonel

    @NotBlank(message = "Sevk nedeni boş olamaz.")
    private String sevkNedeni;

    // 'durum' alanı oluştururken varsayılan olarak "PLANLANDI" olacak, DTO'da almaya gerek yok.
}