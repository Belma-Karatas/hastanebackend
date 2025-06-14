package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.ReceteGoruntuleDTO;
import com.hastane.hastanebackend.dto.ReceteOlusturDTO;
import com.hastane.hastanebackend.dto.ReceteIlacDetayDTO; // ReceteIlacEkleDTO yerine bunu kullanacağız

import java.util.List;
import java.util.Optional;

public interface ReceteService {

    /**
     * Yeni bir reçete oluşturur.
     * Bu işlem genellikle bir muayene sonrası doktor tarafından yapılır.
     *
     * @param receteOlusturDTO Reçete oluşturma bilgilerini ve eklenecek ilaçları içeren DTO.
     * @param doktorKullaniciId İşlemi yapan doktorun Kullanici ID'si.
     * @return Oluşturulan reçetenin detaylarını içeren DTO.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Muayene, hasta veya ilaç bulunamazsa.
     * @throws IllegalArgumentException Geçersiz veri veya iş kuralı ihlali (örn: muayeneye zaten reçete yazılmışsa).
     * @throws org.springframework.security.access.AccessDeniedException İşlemi yapan kişi doktor değilse.
     */
    ReceteGoruntuleDTO createRecete(ReceteOlusturDTO receteOlusturDTO, Integer doktorKullaniciId);

    /**
     * Belirli bir ID'ye sahip reçeteyi görüntüler.
     * Yetki kontrolü yapılır (ilgili hasta, doktor veya admin).
     *
     * @param receteId Görüntülenecek reçetenin ID'si.
     * @param talepEdenKullaniciId Reçeteyi görüntülemek isteyen kullanıcının ID'si.
     * @return Reçete detaylarını içeren DTO veya bulunamazsa Optional.empty().
     */
    Optional<ReceteGoruntuleDTO> getReceteById(Integer receteId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir muayeneye ait reçeteleri (genellikle tek olur) listeler.
     *
     * @param muayeneId Muayene ID'si.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Muayeneye ait ReçeteGoruntuleDTO listesi.
     */
    List<ReceteGoruntuleDTO> getRecetelerByMuayeneId(Integer muayeneId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın tüm reçetelerini listeler.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Hastaya ait ReçeteGoruntuleDTO listesi.
     */
    List<ReceteGoruntuleDTO> getRecetelerByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Mevcut bir reçeteye yeni bir ilaç ekler.
     *
     * @param receteId          Güncellenecek reçetenin ID'si.
     * @param ilacDetayDTO      Eklenecek ilacın bilgilerini (ilaç ID, kullanım şekli vb.) içeren DTO.
     * @param doktorKullaniciId İşlemi yapan doktorun Kullanici ID'si.
     * @return Güncellenmiş reçetenin detaylarını içeren DTO.
     */
    ReceteGoruntuleDTO addIlacToRecete(Integer receteId, ReceteIlacDetayDTO ilacDetayDTO, Integer doktorKullaniciId); // DTO tipi düzeltildi

    /**
     * Mevcut bir reçeteden bir ilacı (ReceteIlac kaydını) siler.
     *
     * @param receteId          İlacın silineceği reçetenin ID'si.
     * @param receteIlacId      Silinecek ReceteIlac kaydının ID'si.
     * @param doktorKullaniciId İşlemi yapan doktorun Kullanici ID'si.
     * @return Güncellenmiş reçetenin detaylarını içeren DTO.
     */
    ReceteGoruntuleDTO removeIlacFromRecete(Integer receteId, Integer receteIlacId, Integer doktorKullaniciId);

    /**
     * Bir reçeteyi siler (Genellikle önerilmez, bunun yerine durumu "İPTAL" vb. olarak güncellenebilir).
     * Proje gereksinimine göre bu metot eklenebilir veya çıkarılabilir.
     *
     * @param receteId Silinecek reçetenin ID'si.
     * @param doktorKullaniciId İşlemi yapan doktorun (veya adminin) Kullanici ID'si.
     */
    // void deleteRecete(Integer receteId, Integer doktorKullaniciId); // Şimdilik yorumda kalsın
}