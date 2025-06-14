package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AcilDurumKaydiOlusturDTO {

    @NotBlank(message = "Açıklama boş olamaz.")
    private String aciklama;

    @NotBlank(message = "Konum boş olamaz.")
    @Size(max = 255, message = "Konum en fazla 255 karakter olabilir.")
    private String konum;

    @NotNull(message = "Olay zamanı boş olamaz.")
    private LocalDateTime olayZamani; // Hemşire tarafından girilecek

    // tetikleyenPersonelId, işlemi yapan hemşirenin Kullanici ID'sinden (JWT) alınacak.

    @Positive(message = "Hasta ID pozitif bir sayı olmalıdır (eğer varsa).")
    private Integer hastaId; // Opsiyonel, her acil durum bir hastaya bağlı olmayabilir

    // 'durum' alanı oluşturulurken varsayılan olarak "AKTIF" olacak, DTO'da almaya gerek yok.
}