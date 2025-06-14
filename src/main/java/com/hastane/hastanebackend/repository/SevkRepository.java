package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Sevk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SevkRepository extends JpaRepository<Sevk, Integer> {

    /**
     * Belirli bir hastaya ait tüm sevk kayıtlarını sevk tarihine göre en yeniden eskiye sıralı bulur.
     *
     * @param hastaId Hasta ID'si.
     * @return Belirtilen hastaya ait sevklerin listesi.
     */
    List<Sevk> findByHasta_IdOrderBySevkTarihiDesc(Integer hastaId);

    /**
     * Belirli bir doktor tarafından yapılmış tüm sevkleri sevk tarihine göre en yeniden eskiye sıralı bulur.
     *
     * @param doktorPersonelId Sevk eden doktorun Personel ID'si.
     * @return Belirtilen doktora ait sevklerin listesi.
     */
    List<Sevk> findBySevkEdenDoktor_IdOrderBySevkTarihiDesc(Integer doktorPersonelId);

    /**
     * Belirli bir durumdaki tüm sevk kayıtlarını listeler (örn: "PLANLANDI").
     * Sonuçlar sevk tarihine göre en yeniden eskiye sıralanır.
     *
     * @param durum Sevk durumu.
     * @return Belirtilen durumdaki sevklerin listesi.
     */
    List<Sevk> findByDurumOrderBySevkTarihiDesc(String durum);

    /**
     * Belirli bir tarih aralığında oluşturulmuş/planlanmış sevkleri listeler.
     * Sonuçlar sevk tarihine göre en yeniden eskiye sıralanır.
     *
     * @param baslangicTarihi Başlangıç tarihi ve saati (dahil).
     * @param bitisTarihi     Bitiş tarihi ve saati (dahil).
     * @return Belirtilen tarih aralığındaki sevklerin listesi.
     */
    List<Sevk> findBySevkTarihiBetweenOrderBySevkTarihiDesc(LocalDateTime baslangicTarihi, LocalDateTime bitisTarihi);

    /**
     * Hedef kurum adında belirli bir anahtar kelime geçen sevkleri bulur (büyük/küçük harf duyarsız).
     * Sonuçlar sevk tarihine göre en yeniden eskiye sıralanır.
     *
     * @param hedefKurumKeyword Aranan anahtar kelime.
     * @return Eşleşen sevklerin listesi.
     */
    List<Sevk> findByHedefKurumContainingIgnoreCaseOrderBySevkTarihiDesc(String hedefKurumKeyword);

}