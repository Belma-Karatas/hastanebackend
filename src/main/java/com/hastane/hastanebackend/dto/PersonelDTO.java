package com.hastane.hastanebackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // Boş constructor için
import lombok.AllArgsConstructor; // Tüm alanları içeren constructor için (opsiyonel)

import java.util.Set;

/**
 * Yeni bir personel oluşturmak veya güncellemek için frontend'den gelen verileri taşıyan nesne (Data Transfer Object).
 * Personel listelemelerinde (özellikle doktorlar) frontend'e gönderilirken de kullanılır.
 */
@Getter
@Setter
@NoArgsConstructor    // Varsayılan olarak boş constructor ekleyelim
@AllArgsConstructor   // Tüm alanları içeren constructor (opsiyonel, ihtiyaç duyarsanız)
public class PersonelDTO {

    private Integer id; // Personel ID'si (listelemelerde frontend için önemli)

    // Personel Bilgileri
    private String ad;
    private String soyad;
    private String telefon;

    // İlişkili Departman
    private Integer departmanId;    // Oluşturma/güncelleme için
    private String departmanAdi;    // Listelemelerde göstermek için

    // Kullanıcı Bilgileri
    private String email;
    private String sifre;           // Sadece oluşturma/güncelleme için kullanılır, listelemelerde gönderilmemeli

    // Kullanıcıya atanacak/atanmış rollerin adları
    private Set<String> roller;

    // Doktor ise, atanacak Branş
    private Integer bransId;        // Oluşturma/güncelleme için
    private String bransAdi;        // Listelemelerde göstermek için
}