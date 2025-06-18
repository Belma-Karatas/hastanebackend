package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.RandevuGoruntuleDTO;
import com.hastane.hastanebackend.dto.RandevuOlusturDTO;
// import com.hastane.hastanebackend.entity.Randevu; // Gerekirse implementasyonda kullanılır
import com.hastane.hastanebackend.exception.ResourceNotFoundException; // Javadoc için eklendi
// import org.springframework.security.access.AccessDeniedException; // Javadoc için SecurityException yerine kullanılabilir

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RandevuService {

    /**
     * Yeni bir randevu oluşturur.
     *
     * @param randevuOlusturDTO Randevu oluşturma bilgilerini içeren DTO.
     * @param talepEdenKullaniciId Randevuyu oluşturan kullanıcının (hasta veya yetkili personel) ID'si.
     * @return Oluşturulan randevunun DTO'su.
     * @throws ResourceNotFoundException Eğer hasta veya doktor bulunamazsa.
     * @throws IllegalArgumentException Eğer randevu saati uygun değilse veya başka bir iş kuralı ihlali varsa.
     */
    RandevuGoruntuleDTO randevuOlustur(RandevuOlusturDTO randevuOlusturDTO, Integer talepEdenKullaniciId);

    /**
     * Belirli bir ID'ye sahip randevuyu görüntüler.
     *
     * @param randevuId Görüntülenecek randevunun ID'si.
     * @param talepEdenKullaniciId Randevuyu görüntülemek isteyen kullanıcının ID'si (yetki kontrolü için).
     * @return Randevu DTO'su veya bulunamazsa Optional.empty().
     * @throws ResourceNotFoundException Eğer randevu veya talep eden kullanıcı bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException Kullanıcının bu randevuyu görüntüleme yetkisi yoksa.
     */
    Optional<RandevuGoruntuleDTO> getRandevuById(Integer randevuId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın tüm randevularını listeler.
     * (Geçmiş, bugün ve gelecek tüm randevuları kapsayabilir, implementasyona göre sıralanır.)
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si (yetki kontrolü için).
     * @return Hastaya ait RandevuGoruntuleDTO listesi.
     * @throws ResourceNotFoundException Eğer hasta veya talep eden kullanıcı bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException Kullanıcının bu hastanın randevularını görüntüleme yetkisi yoksa.
     */
    List<RandevuGoruntuleDTO> getRandevularByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir doktorun belirli bir gündeki randevularını listeler.
     *
     * @param doktorId Doktor (Personel) ID'si.
     * @param gun Randevuların alınacağı gün.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si (yetki kontrolü için).
     * @return Doktora ait RandevuGoruntuleDTO listesi.
     * @throws ResourceNotFoundException Eğer doktor veya talep eden kullanıcı bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException Kullanıcının bu doktorun randevularını görüntüleme yetkisi yoksa.
     */
    List<RandevuGoruntuleDTO> getRandevularByDoktorIdAndGun(Integer doktorId, LocalDate gun, Integer talepEdenKullaniciId);

    /**
     * **YENİ EKLENEN METOT**
     * Belirli bir doktorun tüm randevularını (geçmiş, bugün, gelecek) listeler.
     *
     * @param doktorId Doktor (Personel) ID'si.
     * @return Doktora ait tüm RandevuGoruntuleDTO listesi, genellikle tarihe göre sıralı.
     * @throws ResourceNotFoundException Eğer doktor bulunamazsa.
     */
    List<RandevuGoruntuleDTO> getTumRandevularByDoktorId(Integer doktorId);


    /**
     * Bir randevunun durumunu günceller.
     *
     * @param randevuId Güncellenecek randevunun ID'si.
     * @param yeniDurum Randevunun yeni durumu (Örn: "TAMAMLANDI", "IPTAL EDILDI").
     * @param talepEdenKullaniciId İşlemi yapan kullanıcının ID'si (yetki kontrolü için).
     * @return Güncellenmiş randevunun DTO'su.
     * @throws ResourceNotFoundException Eğer randevu veya talep eden kullanıcı bulunamazsa.
     * @throws IllegalArgumentException Eğer yeni durum geçersizse veya durum geçişi mantığa aykırıysa.
     * @throws org.springframework.security.access.AccessDeniedException Kullanıcının bu işlemi yapma yetkisi yoksa.
     */
    RandevuGoruntuleDTO randevuDurumGuncelle(Integer randevuId, String yeniDurum, Integer talepEdenKullaniciId);

    /**
     * Bir randevuyu iptal eder (Durumunu 'IPTAL EDILDI' yapar).
     *
     * @param randevuId İptal edilecek randevunun ID'si.
     * @param talepEdenKullaniciId İşlemi yapan kullanıcının ID'si (yetki kontrolü için).
     * @return İptal edilen randevunun DTO'su.
     * @throws ResourceNotFoundException Eğer randevu veya talep eden kullanıcı bulunamazsa.
     * @throws IllegalArgumentException Eğer randevu zaten iptal edilmişse veya iptal edilemeyecek bir durumdaysa.
     * @throws org.springframework.security.access.AccessDeniedException Kullanıcının bu işlemi yapma yetkisi yoksa.
     */
    RandevuGoruntuleDTO randevuIptalEt(Integer randevuId, Integer talepEdenKullaniciId);

    // İleride eklenebilecek diğer metotlar:
    // - List<String> getDoktorunUygunSaatleri(Integer doktorId, LocalDate gun);
}