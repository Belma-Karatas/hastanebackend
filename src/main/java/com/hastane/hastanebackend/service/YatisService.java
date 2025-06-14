package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.HemsireAtaDTO; // Bu importu ekle
import com.hastane.hastanebackend.dto.YatisGoruntuleDTO;
import com.hastane.hastanebackend.dto.YatisOlusturDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface YatisService {

    /**
     * Yeni bir hasta yatış kaydı oluşturur.
     * İlgili yatağın 'doluMu' durumunu true olarak günceller.
     * Hastanın zaten aktif bir yatışı olup olmadığını kontrol eder.
     * Yatağın boş olup olmadığını kontrol eder.
     *
     * @param yatisOlusturDTO Yatış bilgilerini içeren DTO.
     * @param yapanKullaniciId İşlemi yapan kullanıcının (örn: kabul görevlisi, hemşire) ID'si.
     * @return Oluşturulan yatışın detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Hasta, yatak veya sorumlu doktor bulunamazsa.
     * @throws IllegalStateException Eğer hasta zaten yatıyorsa veya seçilen yatak doluysa.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    YatisGoruntuleDTO hastaYatisiYap(YatisOlusturDTO yatisOlusturDTO, Integer yapanKullaniciId);

    /**
     * Bir hastanın yatış kaydını sonlandırır (taburcu eder).
     * İlgili yatağın 'doluMu' durumunu false olarak günceller.
     * Çıkış tarihini mevcut zaman olarak ayarlar.
     *
     * @param yatisId         Taburcu edilecek yatışın ID'si.
     * @param yapanKullaniciId İşlemi yapan kullanıcının (örn: doktor, hemşire) ID'si.
     * @return Güncellenmiş yatışın detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Yatış kaydı bulunamazsa.
     * @throws IllegalStateException Eğer yatış zaten taburcu edilmişse.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    YatisGoruntuleDTO hastaTaburcuEt(Integer yatisId, Integer yapanKullaniciId);
    // Alternatif: YatisGoruntuleDTO hastaTaburcuEt(Integer yatisId, LocalDateTime cikisTarihi, Integer yapanKullaniciId);

    /**
     * Verilen ID'ye sahip yatış kaydını DTO olarak bulur.
     *
     * @param yatisId Aranacak yatışın ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si (yetki kontrolü için).
     * @return Bulunursa YatisGoruntuleDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<YatisGoruntuleDTO> getYatisById(Integer yatisId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın tüm yatış kayıtlarını (aktif ve geçmiş) DTO olarak listeler.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen hastaya ait YatisGoruntuleDTO listesi.
     */
    List<YatisGoruntuleDTO> getTumYatislarByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın aktif (çıkış tarihi null olan) yatış kaydını DTO olarak bulur.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Aktif yatış varsa YatisGoruntuleDTO içeren Optional, yoksa boş Optional.
     */
    Optional<YatisGoruntuleDTO> getAktifYatisByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir yatakta aktif (çıkış tarihi null olan) yatış varsa onu DTO olarak bulur.
     *
     * @param yatakId Yatak ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Aktif yatış varsa YatisGoruntuleDTO içeren Optional, yoksa boş Optional.
     */
    Optional<YatisGoruntuleDTO> getAktifYatisByYatakId(Integer yatakId, Integer talepEdenKullaniciId);


    /**
     * Halen hastanede yatan (çıkış tarihi null olan) tüm hastaların yatışlarını DTO olarak listeler.
     *
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Aktif yatışların YatisGoruntuleDTO listesi.
     */
    List<YatisGoruntuleDTO> getTumAktifYatislar(Integer talepEdenKullaniciId);

    // İleride: Yatış detaylarını güncelleme (örn: sorumlu doktor değişikliği, yatış nedeni güncelleme)
    // YatisGoruntuleDTO updateYatisDetaylari(Integer yatisId, YatisGuncelleDTO yatisGuncelleDTO, Integer yapanKullaniciId);

    // YENİ EKLENEN METOTLAR:
    /**
     * Belirli bir yatışa bir hemşire atar.
     * Sadece ADMIN rolüne sahip kullanıcılar bu işlemi yapabilir (senin isteğin üzerine).
     *
     * @param yatisId           Hemşirenin atanacağı yatışın ID'si.
     * @param hemsireAtaDTO     Atanacak hemşirenin personel ID'sini içeren DTO.
     * @param yapanKullaniciId  İşlemi yapan ADMIN kullanıcısının ID'si.
     * @return Güncellenmiş yatışın detaylarını içeren DTO (atanmış hemşire listesiyle birlikte).
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Yatış veya hemşire personel bulunamazsa.
     * @throws IllegalArgumentException Eğer hemşire zaten bu yatışa atanmışsa veya atanan personel hemşire rolünde değilse.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    YatisGoruntuleDTO hemsireAta(Integer yatisId, HemsireAtaDTO hemsireAtaDTO, Integer yapanKullaniciId);

    /**
     * Belirli bir yatıştaki bir hemşire atamasını kaldırır (YatisHemsireAtama kaydını siler).
     * Sadece ADMIN rolüne sahip kullanıcılar bu işlemi yapabilir (senin isteğin üzerine).
     *
     * @param yatisId                   Hemşire atamasının kaldırılacağı yatışın ID'si.
     * @param yatisHemsireAtamaId       Kaldırılacak YatisHemsireAtama kaydının ID'si.
     * @param yapanKullaniciId          İşlemi yapan ADMIN kullanıcısının ID'si.
     * @return Güncellenmiş yatışın detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Yatış veya YatisHemsireAtama kaydı bulunamazsa.
     * @throws IllegalArgumentException Eğer YatisHemsireAtama kaydı belirtilen yatışa ait değilse.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    YatisGoruntuleDTO hemsireAtamasiniKaldir(Integer yatisId, Integer yatisHemsireAtamaId, Integer yapanKullaniciId);
}