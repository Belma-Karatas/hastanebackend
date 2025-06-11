package com.hastane.hastanebackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class HastaKayitDTO {
    // Bu alanlar hem Kullanici hem de Hasta entity'sine yazılacak
    private String ad;
    private String soyad;
   

    // Kullanici entity'si için
    private String email;
    private String sifre;

    // Hasta entity'si için
    private String tcKimlikNo;
    private LocalDate dogumTarihi;
    private String cinsiyet;
}