package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KatDTO {

    private Integer id; // Görüntüleme ve güncelleme için kullanılabilir

    @NotBlank(message = "Kat adı boş olamaz.")
    @Size(max = 50, message = "Kat adı en fazla 50 karakter olabilir.")
    private String ad;

    
    

    // Sadece oluşturma için constructor (ID olmadan)
    public KatDTO(String ad) {
        this.ad = ad;
    }
}