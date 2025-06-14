package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.RandevuGoruntuleDTO;
import com.hastane.hastanebackend.dto.RandevuOlusturDTO;
import com.hastane.hastanebackend.entity.Randevu; // Entity'yi de import edebiliriz, bazen içsel olarak gerekebilir

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
     */
    Optional<RandevuGoruntuleDTO> getRandevuById(Integer randevuId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın tüm randevularını listeler.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Hastaya ait RandevuGoruntuleDTO listesi.
     */
    List<RandevuGoruntuleDTO> getRandevularByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir doktorun belirli bir gündeki randevularını listeler.
     *
     * @param doktorId Doktor (Personel) ID'si.
     * @param gun Randevuların alınacağı gün.
     * @param talepEdenKullaniciId İstekte bulunan kullanıcının ID'si.
     * @return Doktora ait RandevuGoruntuleDTO listesi.
     */
    List<RandevuGoruntuleDTO> getRandevularByDoktorIdAndGun(Integer doktorId, LocalDate gun, Integer talepEdenKullaniciId);

    /**
     * Bir randevuyu günceller (örneğin durumunu).
     * Bu metot daha detaylandırılabilir (örn: hangi alanların güncellenebileceği).
     * Şimdilik sadece durumu değiştirmek için basit bir versiyon düşünebiliriz.
     *
     * @param randevuId Güncellenecek randevunun ID'si.
     * @param yeniDurum Randevunun yeni durumu.
     * @param talepEdenKullaniciId İşlemi yapan kullanıcının ID'si.
     * @return Güncellenmiş randevunun DTO'su.
     * @throws ResourceNotFoundException Eğer randevu bulunamazsa.
     * @throws SecurityException Eğer kullanıcının bu işlemi yapma yetkisi yoksa.
     */
    RandevuGoruntuleDTO randevuDurumGuncelle(Integer randevuId, String yeniDurum, Integer talepEdenKullaniciId);

    /**
     * Bir randevuyu iptal eder. (Durumunu 'IPTAL EDILDI' yapar)
     *
     * @param randevuId İptal edilecek randevunun ID'si.
     * @param talepEdenKullaniciId İşlemi yapan kullanıcının ID'si.
     * @return İptal edilen randevunun DTO'su.
     */
    RandevuGoruntuleDTO randevuIptalEt(Integer randevuId, Integer talepEdenKullaniciId);

    // İleride eklenebilecek diğer metotlar:
    // - Bir doktorun belirli bir zaman aralığındaki boş saatlerini getirme
    // - Randevu hatırlatma servisi (Bu daha çok bir görev olur)
}