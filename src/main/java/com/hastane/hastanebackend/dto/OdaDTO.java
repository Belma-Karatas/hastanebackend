package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // Builder ekleyelim

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Servis katmanında DTO oluşturmayı kolaylaştırır
public class OdaDTO {

    private Integer id; // Oda ID'si (görüntüleme, güncelleme için)

    @NotBlank(message = "Oda numarası boş olamaz.")
    @Size(max = 20, message = "Oda numarası en fazla 20 karakter olabilir.")
    private String odaNumarasi;

    @NotNull(message = "Kat ID boş olamaz.")
    @Positive(message = "Kat ID pozitif bir sayı olmalıdır.")
    private Integer katId; // Bu oda hangi kata ait (Sadece ID'sini alıyoruz)

    // Görüntüleme için kat adını da DTO'ya ekleyebiliriz
    private String katAdi; // Bu alan entity'den DTO'ya dönüşüm sırasında doldurulacak

    @NotNull(message = "Kapasite boş olamaz.")
    @Positive(message = "Kapasite pozitif bir sayı olmalıdır.")
    private Integer kapasite;

    // Odanın mevcut yatak sayısı veya dolu/boş yatak sayısı gibi bilgiler de
    // ileride bu DTO'ya eklenebilir, ancak şimdilik temel alanlarla başlayalım.
    // private Integer mevcutYatakSayisi;
}