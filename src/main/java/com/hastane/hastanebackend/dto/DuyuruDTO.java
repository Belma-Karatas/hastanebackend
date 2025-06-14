package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DuyuruDTO {

    private Integer id;

    @NotBlank(message = "Duyuru başlığı boş olamaz.")
    @Size(max = 255, message = "Duyuru başlığı en fazla 255 karakter olabilir.")
    private String baslik;

    @NotBlank(message = "Duyuru içeriği boş olamaz.")
    private String icerik;

    private LocalDateTime yayinTarihi; // Görüntüleme için

    private Integer yayinlayanPersonelId; // Oluştururken sadece ID alınabilir
    private String yayinlayanPersonelAdiSoyadi; // Görüntüleme için
}