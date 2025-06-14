package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YatakDTO {

    private Integer id; // Yatak ID'si

    @NotBlank(message = "Yatak numarası boş olamaz.")
    @Size(max = 20, message = "Yatak numarası en fazla 20 karakter olabilir.")
    private String yatakNumarasi;

    @NotNull(message = "Oda ID boş olamaz.")
    @Positive(message = "Oda ID pozitif bir sayı olmalıdır.")
    private Integer odaId; // Bu yatak hangi odaya ait

    // Görüntüleme için oda ve kat bilgilerini de DTO'ya ekleyebiliriz
    private String odaNumarasiOdaDto; // Oda entity'sinden gelen oda numarası (karışmaması için farklı isimlendirdim)
    private String katAdi;           // Kat entity'sinden gelen kat adı

    @NotNull(message = "Dolu/Boş durumu belirtilmelidir.")
    private Boolean doluMu;

    // İleride yatakta yatan hastanın ID'si veya adı gibi bilgiler eklenebilir
    // private Integer hastaId;
    // private String hastaAdiSoyadi;
}