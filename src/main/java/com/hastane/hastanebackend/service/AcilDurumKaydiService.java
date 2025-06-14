package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.AcilDurumKaydiGuncelleDTO;
import com.hastane.hastanebackend.dto.AcilDurumKaydiGoruntuleDTO;
import com.hastane.hastanebackend.dto.AcilDurumKaydiOlusturDTO;

import java.time.LocalDate; // Tarihe göre filtreleme için
import java.util.List;
import java.util.Optional;

public interface AcilDurumKaydiService {

    /**
     * Yeni bir acil durum kaydı oluşturur.
     * Bu işlem sadece HEMŞİRE tarafından yapılır.
     *
     * @param acilDurumKaydiOlusturDTO Acil durum kaydı oluşturma bilgilerini içeren DTO.
     * @param tetikleyenHemsireKullaniciId İşlemi yapan hemşirenin Kullanici ID'si.
     * @return Oluşturulan acil durum kaydının detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Hasta (eğer belirtilmişse) veya tetikleyen hemşire personel bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapan kişi hemşire değilse.
     */
    AcilDurumKaydiGoruntuleDTO createAcilDurumKaydi(AcilDurumKaydiOlusturDTO acilDurumKaydiOlusturDTO, Integer tetikleyenHemsireKullaniciId);

    /**
     * Bir acil durum kaydının durumunu günceller (örn: AKTIF -> MÜDAHALE EDİLİYOR -> SONLANDIRILDI).
     * Bu işlem yetkili personel (örn: sorumlu doktor, yönetici hemşire, ADMIN) tarafından yapılır.
     *
     * @param kayitId               Güncellenecek acil durum kaydının ID'si.
     * @param guncelleDTO           Yeni durumu (ve opsiyonel notları) içeren DTO.
     * @param yapanKullaniciId      İşlemi yapan yetkili kullanıcının ID'si.
     * @return Güncellenmiş acil durum kaydının detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Acil durum kaydı bulunamazsa.
     * @throws IllegalArgumentException Geçersiz durum veya durum geçişi.
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapmaya yetkisi yoksa.
     */
    AcilDurumKaydiGoruntuleDTO updateAcilDurumKaydiDurumu(Integer kayitId, AcilDurumKaydiGuncelleDTO guncelleDTO, Integer yapanKullaniciId);

    /**
     * Verilen ID'ye sahip acil durum kaydını DTO olarak bulur.
     *
     * @param kayitId              Aranacak kaydın ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si (yetki kontrolü için).
     * @return Bulunursa AcilDurumKaydiGoruntuleDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<AcilDurumKaydiGoruntuleDTO> getAcilDurumKaydiById(Integer kayitId, Integer talepEdenKullaniciId);

    /**
     * Tüm acil durum kayıtlarını (genellikle olay zamanına göre en yeniden eskiye) DTO olarak listeler.
     * Genellikle ADMIN veya YONETICI gibi roller tarafından erişilir.
     *
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return AcilDurumKaydiGoruntuleDTO nesnelerinden oluşan bir liste.
     */
    List<AcilDurumKaydiGoruntuleDTO> getAllAcilDurumKayitlari(Integer talepEdenKullaniciId);

    /**
     * Belirli bir durumdaki tüm acil durum kayıtlarını DTO olarak listeler.
     *
     * @param durum                Filtrelenecek durum (örn: "AKTIF").
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen durumdaki AcilDurumKaydiGoruntuleDTO listesi.
     */
    List<AcilDurumKaydiGoruntuleDTO> getAcilDurumKayitlariByDurum(String durum, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastaya ait tüm acil durum kayıtlarını DTO olarak listeler.
     *
     * @param hastaId              Hasta ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen hastaya ait AcilDurumKaydiGoruntuleDTO listesi.
     */
    List<AcilDurumKaydiGoruntuleDTO> getAcilDurumKayitlariByHastaId(Integer hastaId, Integer talepEdenKullaniciId);
    
    /**
     * Belirli bir tarihte meydana gelmiş acil durum kayıtlarını listeler.
     *
     * @param tarih Sorgulanacak tarih.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen tarihteki AcilDurumKaydiGoruntuleDTO listesi.
     */
    List<AcilDurumKaydiGoruntuleDTO> getAcilDurumKayitlariByTarih(LocalDate tarih, Integer talepEdenKullaniciId);
}