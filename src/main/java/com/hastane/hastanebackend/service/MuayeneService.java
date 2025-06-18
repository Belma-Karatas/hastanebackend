package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.MuayeneGoruntuleDTO;
import com.hastane.hastanebackend.dto.MuayeneOlusturDTO;
// Gerekli importlar
import com.hastane.hastanebackend.exception.ResourceNotFoundException; // ResourceNotFoundException için
import org.springframework.security.access.AccessDeniedException; // AccessDeniedException için

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MuayeneService {

    /**
     * Yeni bir muayene kaydı oluşturur.
     *
     * @param muayeneOlusturDTO Muayene oluşturma bilgilerini içeren DTO.
     * @param doktorKullaniciId Muayeneyi kaydeden doktorun Kullanici ID'si.
     * @return Oluşturulan muayenenin DTO'su.
     * @throws ResourceNotFoundException Eğer hasta, doktor veya ilişkili randevu (verilmişse) bulunamazsa.
     * @throws IllegalArgumentException Geçersiz veri veya iş kuralı ihlali durumunda.
     * @throws AccessDeniedException Eğer işlemi yapan kişi doktor değilse veya yetkisi yoksa.
     */
    MuayeneGoruntuleDTO muayeneOlustur(MuayeneOlusturDTO muayeneOlusturDTO, Integer doktorKullaniciId);

    /**
     * Belirli bir ID'ye sahip muayeneyi DTO olarak görüntüler.
     *
     * @param muayeneId Görüntülenecek muayenenin ID'si.
     * @param talepEdenKullaniciId Muayeneyi görüntülemek isteyen kullanıcının ID'si (yetki kontrolü için).
     * @return Muayene DTO'su veya bulunamazsa Optional.empty().
     * @throws ResourceNotFoundException Muayene veya talep eden kullanıcı bulunamazsa.
     * @throws AccessDeniedException Kullanıcının bu muayeneyi görüntüleme yetkisi yoksa.
     */
    Optional<MuayeneGoruntuleDTO> getMuayeneById(Integer muayeneId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir randevuya ait (varsa) muayene kaydını DTO olarak bulur.
     *
     * @param randevuId İlişkili randevunun ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si (yetki kontrolü için).
     * @return Muayene DTO'su veya bulunamazsa Optional.empty().
     * @throws ResourceNotFoundException Randevu veya talep eden kullanıcı bulunamazsa.
     * @throws AccessDeniedException Kullanıcının bu randevuya ait muayeneyi görüntüleme yetkisi yoksa.
     */
    Optional<MuayeneGoruntuleDTO> findDtoByRandevuId(Integer randevuId, Integer talepEdenKullaniciId); // <<-- YENİ EKLENEN METOT TANIMI

    /**
     * Belirli bir hastanın tüm muayenelerini listeler.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Hastaya ait MuayeneGoruntuleDTO listesi.
     * @throws ResourceNotFoundException Hasta veya talep eden kullanıcı bulunamazsa.
     * @throws AccessDeniedException Kullanıcının bu hastanın muayenelerini görüntüleme yetkisi yoksa.
     */
    List<MuayeneGoruntuleDTO> getMuayenelerByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir doktorun belirli bir gündeki muayenelerini listeler.
     *
     * @param doktorPersonelId Doktor (Personel) ID'si.
     * @param gun Muayenelerin alınacağı gün.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Doktora ait MuayeneGoruntuleDTO listesi.
     * @throws ResourceNotFoundException Doktor veya talep eden kullanıcı bulunamazsa.
     * @throws AccessDeniedException Kullanıcının bu doktorun muayenelerini görüntüleme yetkisi yoksa.
     */
    List<MuayeneGoruntuleDTO> getMuayenelerByDoktorIdAndGun(Integer doktorPersonelId, LocalDate gun, Integer talepEdenKullaniciId);

    /**
     * Bir muayene kaydını günceller.
     *
     * @param muayeneId Güncellenecek muayenenin ID'si.
     * @param guncelMuayeneDTO Güncel bilgileri içeren DTO.
     * @param doktorKullaniciId İşlemi yapan doktorun Kullanici ID'si.
     * @return Güncellenmiş muayenenin DTO'su.
     * @throws ResourceNotFoundException Muayene veya aktif kullanıcı bulunamazsa.
     * @throws IllegalArgumentException Geçersiz veri veya iş kuralı ihlali.
     * @throws AccessDeniedException Kullanıcının bu muayeneyi güncelleme yetkisi yoksa.
     */
    MuayeneGoruntuleDTO muayeneGuncelle(Integer muayeneId, MuayeneOlusturDTO guncelMuayeneDTO, Integer doktorKullaniciId);

}