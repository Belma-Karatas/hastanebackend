package com.hastane.hastanebackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RandevuGoruntuleDTO {
    private Integer id;
    private LocalDateTime randevuTarihiSaati;
    private String durum;
    private String hastaAdiSoyadi;    // Hasta Adı Soyadı (Hasta entity'sinden birleştirilecek)
    private String doktorAdiSoyadi;   // Doktor Adı Soyadı (Personel entity'sinden birleştirilecek)
    private String doktorBransAdi;    // Doktorun Branşı (DoktorDetay -> Brans üzerinden)
    private Integer hastaId;          // Hasta ID'si
    private Integer doktorId;         // Doktor (Personel) ID'si

    // Daha fazla detay eklenebilir, örneğin:
    // private LocalDateTime olusturulmaTarihi;
    // private String hastaTelefon;
    // private String doktorTelefon;

    // Constructor'lar, builder pattern vs. eklenebilir. Şimdilik sadece alanlar yeterli.
    // Eğer bu DTO'yu oluşturmak için bir mapper/converter sınıfı kullanacaksak,
    // o zaman constructor veya static factory metotları faydalı olabilir.
}