package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.MuayeneGoruntuleDTO;
import com.hastane.hastanebackend.dto.MuayeneOlusturDTO;

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
     * @throws ResourceNotFoundException Eğer hasta veya ilişkili randevu (verilmişse) bulunamazsa.
     * @throws IllegalArgumentException Geçersiz veri veya iş kuralı ihlali durumunda.
     * @throws SecurityException Eğer işlemi yapan kişi doktor değilse.
     */
    MuayeneGoruntuleDTO muayeneOlustur(MuayeneOlusturDTO muayeneOlusturDTO, Integer doktorKullaniciId);

    /**
     * Belirli bir ID'ye sahip muayeneyi görüntüler.
     *
     * @param muayeneId Görüntülenecek muayenenin ID'si.
     * @param talepEdenKullaniciId Muayeneyi görüntülemek isteyen kullanıcının ID'si (yetki kontrolü için).
     * @return Muayene DTO'su veya bulunamazsa Optional.empty().
     */
    Optional<MuayeneGoruntuleDTO> getMuayeneById(Integer muayeneId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın tüm muayenelerini listeler.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Hastaya ait MuayeneGoruntuleDTO listesi.
     */
    List<MuayeneGoruntuleDTO> getMuayenelerByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir doktorun belirli bir gündeki muayenelerini listeler.
     *
     * @param doktorPersonelId Doktor (Personel) ID'si.
     * @param gun Muayenelerin alınacağı gün.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Doktora ait MuayeneGoruntuleDTO listesi.
     */
    List<MuayeneGoruntuleDTO> getMuayenelerByDoktorIdAndGun(Integer doktorPersonelId, LocalDate gun, Integer talepEdenKullaniciId);

    /**
     * Bir muayene kaydını günceller.
     * Sadece belirli alanların güncellenmesine izin verilebilir (örn: tanı, tedavi notları).
     *
     * @param muayeneId Güncellenecek muayenenin ID'si.
     * @param guncelMuayeneDTO Güncel bilgileri içeren DTO. (Belki ayrı bir MuayeneGuncelleDTO olabilir)
     * @param doktorKullaniciId İşlemi yapan doktorun Kullanici ID'si.
     * @return Güncellenmiş muayenenin DTO'su.
     */
    MuayeneGoruntuleDTO muayeneGuncelle(Integer muayeneId, MuayeneOlusturDTO guncelMuayeneDTO, Integer doktorKullaniciId);

    // Muayene silme işlemi genellikle yapılmaz, onun yerine 'iptal' veya 'geçersiz' gibi bir durum eklenebilir.
    // Ama proje gereksinimine göre eklenebilir. Şimdilik eklemiyoruz.
    // void muayeneSil(Integer muayeneId, Integer doktorKullaniciId);
}