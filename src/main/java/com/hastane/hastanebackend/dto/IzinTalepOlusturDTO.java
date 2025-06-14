package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class IzinTalepOlusturDTO {

    // talepEdenPersonelId, JWT token'dan veya security context'ten alınacak, DTO'da olmasına gerek yok.

    @NotNull(message = "İzin türü ID boş olamaz.")
    @Positive(message = "İzin türü ID pozitif bir sayı olmalıdır.")
    private Integer izinTuruId;

    @NotNull(message = "Başlangıç tarihi boş olamaz.")
    @FutureOrPresent(message = "Başlangıç tarihi geçmiş bir tarih olamaz.")
    private LocalDate baslangicTarihi;

    @NotNull(message = "Bitiş tarihi boş olamaz.")
    @FutureOrPresent(message = "Bitiş tarihi geçmiş bir tarih olamaz.")
    private LocalDate bitisTarihi;

    // gunSayisi frontend'de hesaplanıp gönderilebilir veya backend'de tarihlerden hesaplanabilir.
    // Şimdilik frontend'den alınacağını varsayalım.
    @NotNull(message = "Gün sayısı boş olamaz.")
    @Positive(message = "Gün sayısı pozitif olmalıdır.")
    private Integer gunSayisi;

    @NotBlank(message = "Açıklama boş olamaz.")
    private String aciklama;
}