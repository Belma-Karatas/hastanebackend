package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.IzinTalepDurumGuncelleDTO;
import com.hastane.hastanebackend.dto.IzinTalepGoruntuleDTO;
import com.hastane.hastanebackend.dto.IzinTalepOlusturDTO;

import java.util.List;
import java.util.Optional;

public interface IzinTalepService {

    /**
     * Yeni bir izin talebi oluşturur.
     * Talep eden personelin kimliği güvenlik bağlamından (JWT) alınır.
     * Tarih çakışması kontrolü yapılır.
     *
     * @param izinTalepOlusturDTO İzin talebi oluşturma bilgilerini içeren DTO.
     * @param talepEdenKullaniciId İzin talebinde bulunan personelin Kullanici ID'si.
     * @return Oluşturulan izin talebinin detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException İzin türü veya talep eden personel bulunamazsa.
     * @throws IllegalArgumentException Başlangıç tarihi bitiş tarihinden sonraysa, gün sayısı hatalıysa veya tarih çakışması varsa.
     */
    IzinTalepGoruntuleDTO createIzinTalep(IzinTalepOlusturDTO izinTalepOlusturDTO, Integer talepEdenKullaniciId);

    /**
     * Bir izin talebinin durumunu günceller (ONAYLANDI veya REDDEDILDI).
     * Sadece ADMIN rolüne sahip kullanıcılar bu işlemi yapabilir.
     *
     * @param izinTalepId         Güncellenecek izin talebinin ID'si.
     * @param durumGuncelleDTO    Yeni durumu içeren DTO.
     * @param onaylayanKullaniciId İşlemi yapan ADMIN kullanıcısının ID'si.
     * @return Güncellenmiş izin talebinin detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException İzin talebi veya onaylayan yönetici bulunamazsa.
     * @throws IllegalStateException Eğer izin talebi zaten işlem görmüşse (onaylanmış/reddedilmişse).
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    IzinTalepGoruntuleDTO updateIzinTalepDurumu(Integer izinTalepId, IzinTalepDurumGuncelleDTO durumGuncelleDTO, Integer onaylayanKullaniciId);

    /**
     * Verilen ID'ye sahip izin talebini DTO olarak bulur.
     * Yetki kontrolü yapılır (sadece talep eden personel veya ADMIN görebilir).
     *
     * @param izinTalepId          Aranacak izin talebinin ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Bulunursa IzinTalepGoruntuleDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<IzinTalepGoruntuleDTO> getIzinTalepById(Integer izinTalepId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir personele ait tüm izin taleplerini DTO olarak listeler.
     * Eğer talep eden kişi ADMIN ise tüm personelin taleplerini getirebilir (opsiyonel, filtreleme ile eklenebilir).
     * Şimdilik, bir personel sadece kendi taleplerini görebilir varsayımıyla gidiyoruz.
     *
     * @param personelKullaniciId İzin talepleri listelenecek personelin Kullanici ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si (yetki kontrolü için).
     * @return Personele ait IzinTalepGoruntuleDTO listesi.
     */
    List<IzinTalepGoruntuleDTO> getIzinTalepleriByPersonel(Integer personelKullaniciId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir durumdaki tüm izin taleplerini DTO olarak listeler (örn: BEKLIYOR).
     * Sadece ADMIN rolüne sahip kullanıcılar bu listeyi görebilir.
     *
     * @param durum               Filtrelenecek izin durumu.
     * @param adminKullaniciId    İşlemi yapan ADMIN kullanıcısının ID'si.
     * @return Belirtilen durumdaki IzinTalepGoruntuleDTO listesi.
     */
    List<IzinTalepGoruntuleDTO> getIzinTalepleriByDurum(String durum, Integer adminKullaniciId);

    /**
     * Tüm izin taleplerini DTO olarak listeler.
     * Sadece ADMIN rolüne sahip kullanıcılar bu listeyi görebilir.
     *
     * @param adminKullaniciId İşlemi yapan ADMIN kullanıcısının ID'si.
     * @return Tüm IzinTalepGoruntuleDTO listesi.
     */
    List<IzinTalepGoruntuleDTO> getAllIzinTalepleri(Integer adminKullaniciId);

    // İzin talebini iptal etme (personel tarafından, belirli koşullarda)
    // void cancelIzinTalep(Integer izinTalepId, Integer talepEdenKullaniciId); // İleride eklenebilir.
}