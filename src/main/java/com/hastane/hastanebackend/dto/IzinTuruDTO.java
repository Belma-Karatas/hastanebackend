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
public class IzinTuruDTO {

    private Integer id;

    @NotBlank(message = "İzin türü adı boş olamaz.")
    @Size(max = 100, message = "İzin türü adı en fazla 100 karakter olabilir.")
    private String ad;

    // Sadece oluşturma için kullanılacak DTO'larda bu constructor'a gerek kalmayabilir
    // @AllArgsConstructor ve @NoArgsConstructor genellikle yeterlidir.
    // public IzinTuruDTO(String ad) {
    //     this.ad = ad;
    // }
}