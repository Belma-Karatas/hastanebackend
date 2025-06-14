package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceteIlacDetayDTO {

    // Reçete oluştururken göndermek için:
    @NotNull(message = "İlaç ID boş olamaz.")
    private Integer ilacId;

    private String kullanimSekli; // Opsiyonel, kullanıcı tarafından girilebilir

    // Reçeteyi görüntülerken doldurmak için (servis katmanında set edilecek):
    private String ilacAdi;      // Ilac entity'sinden alınacak
    private Integer receteIlacId; // Bu ReceteIlac entity'sinin ID'si (reçeteden ilaç silmek için gerekebilir)

    // Sadece oluşturma için kullanılan constructor
    public ReceteIlacDetayDTO(Integer ilacId, String kullanimSekli) {
        this.ilacId = ilacId;
        this.kullanimSekli = kullanimSekli;
    }
}