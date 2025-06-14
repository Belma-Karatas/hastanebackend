package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.PersonelVardiyaGoruntuleDTO;
import com.hastane.hastanebackend.dto.PersonelVardiyaOlusturDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PersonelVardiyaService {

    /**
     * Bir personele belirli bir tarih için vardiya atar.
     * - Personelin o tarihte zaten bir vardiyası olup olmadığını kontrol eder.
     * - Personelin rolüne (Doktor/Hemşire) ve vardiyanın türüne (saatlerine) göre atama kurallarını uygular.
     *   (Doktorlar sadece 09-17, hemşireler 08-17 veya 17-08 gibi)
     * Bu işlem sadece ADMIN tarafından yapılır.
     *
     * @param personelVardiyaOlusturDTO Atama bilgilerini içeren DTO.
     * @param yapanKullaniciId         İşlemi yapan ADMIN kullanıcısının ID'si.
     * @return Oluşturulan personel vardiya atamasının detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Personel veya Vardiya bulunamazsa.
     * @throws IllegalArgumentException Eğer personel o tarihte zaten bir vardiyaya sahipse veya atama kurallara aykırıysa.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    PersonelVardiyaGoruntuleDTO vardiyaAta(PersonelVardiyaOlusturDTO personelVardiyaOlusturDTO, Integer yapanKullaniciId);

    /**
     * Bir personelin belirli bir tarihteki vardiya atamasını kaldırır.
     * Bu işlem sadece ADMIN tarafından yapılır.
     *
     * @param personelVardiyaId      Kaldırılacak personel vardiya atamasının ID'si.
     * @param yapanKullaniciId      İşlemi yapan ADMIN kullanıcısının ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Personel vardiya ataması bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    void vardiyaAtamasiniKaldir(Integer personelVardiyaId, Integer yapanKullaniciId);
    // Alternatif: void vardiyaAtamasiniKaldir(Integer personelId, LocalDate tarih, Integer yapanKullaniciId);


    /**
     * Verilen ID'ye sahip personel vardiya atamasını DTO olarak bulur.
     *
     * @param personelVardiyaId    Aranacak atamanın ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si (yetki kontrolü için).
     * @return Bulunursa PersonelVardiyaGoruntuleDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<PersonelVardiyaGoruntuleDTO> getPersonelVardiyaById(Integer personelVardiyaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir personele ait tüm vardiya atamalarını DTO olarak listeler.
     *
     * @param personelId           Personel ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Personele ait PersonelVardiyaGoruntuleDTO listesi.
     */
    List<PersonelVardiyaGoruntuleDTO> getVardiyalarByPersonelId(Integer personelId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir tarihteki tüm personel vardiya atamalarını DTO olarak listeler.
     *
     * @param tarih                Tarih.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen tarihteki PersonelVardiyaGoruntuleDTO listesi.
     */
    List<PersonelVardiyaGoruntuleDTO> getVardiyalarByTarih(LocalDate tarih, Integer talepEdenKullaniciId);

    /**
     * Belirli bir tarih aralığındaki ve belirli bir personele ait vardiya atamalarını DTO olarak listeler.
     *
     * @param personelId           Personel ID'si.
     * @param baslangicTarihi      Başlangıç tarihi.
     * @param bitisTarihi          Bitiş tarihi.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Kriterlere uyan PersonelVardiyaGoruntuleDTO listesi.
     */
    List<PersonelVardiyaGoruntuleDTO> getPersonelVardiyalariByTarihAraligi(
            Integer personelId, LocalDate baslangicTarihi, LocalDate bitisTarihi, Integer talepEdenKullaniciId);

    /**
     * Giriş yapmış personelin kendi yaklaşan vardiyalarını (örn: bugünden itibaren 1 hafta) listeler.
     *
     * @param talepEdenKullaniciId Giriş yapmış personelin Kullanici ID'si.
     * @return Yaklaşan PersonelVardiyaGoruntuleDTO listesi.
     */
    List<PersonelVardiyaGoruntuleDTO> getMyYaklasanVardiyalar(Integer talepEdenKullaniciId);
}