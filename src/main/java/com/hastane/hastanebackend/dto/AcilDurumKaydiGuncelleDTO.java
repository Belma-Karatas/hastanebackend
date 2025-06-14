package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcilDurumKaydiGuncelleDTO {

    @NotBlank(message = "Yeni durum boş olamaz.")
    @Pattern(regexp = "AKTIF|MÜDAHALE EDİLİYOR|SONLANDIRILDI", message = "Durum 'AKTIF', 'MÜDAHALE EDİLİYOR' veya 'SONLANDIRILDI' olabilir.")
    private String yeniDurum;

    // private String mudahaleNotlari; // Opsiyonel olarak eklenebilir
}