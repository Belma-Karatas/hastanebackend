package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.SevkGoruntuleDTO;
import com.hastane.hastanebackend.dto.SevkOlusturDTO;

import java.time.LocalDateTime; // LocalDateTime importu kalabilir, DTO'larda kullanılıyor.
import java.util.List;
import java.util.Optional;

public interface SevkService {

    /**
     * Yeni bir sevk kaydı oluşturur.
     * Bu işlem SADECE bir DOKTOR tarafından yapılır.
     * Durumu varsayılan olarak "PLANLANDI" set edilir.
     *
     * @param sevkOlusturDTO    Sevk oluşturma bilgilerini içeren DTO.
     * @param sevkEdenDoktorKullaniciId İşlemi yapan doktorun Kullanici ID'si (JWT'den alınır).
     * @return Oluşturulan sevkin detaylarını içeren SevkGoruntuleDTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Hasta veya sevk eden doktor (personel olarak) bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapan kişi doktor değilse veya yetkisi yoksa.
     * @throws IllegalArgumentException Sevk tarihi geçmişse veya diğer DTO validasyon hataları.
     */
    SevkGoruntuleDTO createSevk(SevkOlusturDTO sevkOlusturDTO, Integer sevkEdenDoktorKullaniciId);

    // updateSevkDurumu metodu kaldırıldı.

    /**
     * Verilen ID'ye sahip sevk kaydını DTO olarak bulur.
     *
     * @param sevkId               Aranacak sevkin ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si (yetki kontrolü için).
     * @return Bulunursa SevkGoruntuleDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<SevkGoruntuleDTO> getSevkById(Integer sevkId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastaya ait tüm sevk kayıtlarını DTO olarak listeler.
     *
     * @param hastaId              Hasta ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen hastaya ait SevkGoruntuleDTO listesi.
     */
    List<SevkGoruntuleDTO> getSevklerByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir doktor tarafından oluşturulan tüm sevkleri DTO olarak listeler.
     *
     * @param doktorPersonelId     Sevk eden doktorun Personel ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen doktora ait SevkGoruntuleDTO listesi.
     */
    List<SevkGoruntuleDTO> getSevklerByDoktorId(Integer doktorPersonelId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir durumdaki tüm sevkleri DTO olarak listeler.
     * Genellikle ADMIN veya ilgili birimler tarafından kullanılır.
     * Not: Durum güncelleme kaldırıldığı için bu metot "PLANLANDI" durumundaki sevkleri listelemek için kullanılabilir.
     *
     * @param durum                Filtrelenecek sevk durumu (örn: "PLANLANDI").
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen durumdaki SevkGoruntuleDTO listesi.
     */
    List<SevkGoruntuleDTO> getSevklerByDurum(String durum, Integer talepEdenKullaniciId);

    /**
     * Tüm sevk kayıtlarını DTO olarak listeler.
     * Genellikle ADMIN veya ilgili birimler tarafından kullanılır.
     *
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Tüm SevkGoruntuleDTO listesi.
     */
    List<SevkGoruntuleDTO> getAllSevkler(Integer talepEdenKullaniciId);
}