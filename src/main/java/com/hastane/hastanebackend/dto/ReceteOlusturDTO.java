package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ReceteOlusturDTO {

    @NotNull(message = "Muayene ID boş olamaz.")
    private Integer muayeneId;

    @NotNull(message = "Reçete tarihi boş olamaz.")
    private LocalDate receteTarihi; // Genellikle o günün tarihi olur

    @NotEmpty(message = "Reçetede en az bir ilaç bulunmalıdır.")
    private List<ReceteIlacDetayDTO> ilaclar; // Eklenecek ilaçların listesi
}