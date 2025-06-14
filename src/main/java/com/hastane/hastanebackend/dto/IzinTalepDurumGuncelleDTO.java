package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IzinTalepDurumGuncelleDTO {

    @NotBlank(message = "Durum boş olamaz.")
    @Pattern(regexp = "ONAYLANDI|REDDEDILDI", message = "Durum sadece 'ONAYLANDI' veya 'REDDEDILDI' olabilir.")
    private String yeniDurum;

    // Reddedilirse bir açıklama eklenebilir (opsiyonel)
    // private String yoneticiAciklamasi;
}