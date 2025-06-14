package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Recete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // JPQL sorguları için
import org.springframework.data.repository.query.Param; // Sorgu parametreleri için
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReceteRepository extends JpaRepository<Recete, Integer> {

    /**
     * Belirli bir muayeneye ait tüm reçeteleri bulur.
     * Genellikle bir muayenenin tek bir reçetesi olur ama sistem yapısına göre değişebilir.
     * Eğer bir muayenenin birden fazla reçetesi olabiliyorsa List<Recete> döner.
     * Eğer bir muayenenin en fazla bir reçetesi olabiliyorsa ve muayene_id Recete'de unique ise Optional<Recete> döner.
     * SQL'de muayene_id UNIQUE olmadığı için List<Recete> daha uygun.
     *
     * @param muayeneId Muayene ID'si.
     * @return Belirtilen muayeneye ait reçetelerin listesi.
     */
    List<Recete> findByMuayene_IdOrderByReceteTarihiDesc(Integer muayeneId);

    /**
     * Belirli bir hastaya ait tüm reçeteleri bulur (reçete tarihine göre en yeniden eskiye sıralı).
     * Bu, Muayene entity'si üzerinden Hasta'ya ulaşılarak yapılır.
     *
     * @param hastaId Hasta ID'si.
     * @return Belirtilen hastaya ait reçetelerin listesi.
     */
    List<Recete> findByMuayene_Hasta_IdOrderByReceteTarihiDesc(Integer hastaId);

    /**
     * Belirli bir tarih aralığındaki tüm reçeteleri bulur.
     *
     * @param baslangicTarihi Başlangıç tarihi (dahil).
     * @param bitisTarihi Bitiş tarihi (dahil).
     * @return Belirtilen tarih aralığındaki reçetelerin listesi.
     */
    List<Recete> findByReceteTarihiBetween(LocalDate baslangicTarihi, LocalDate bitisTarihi);

    /**
     * Belirli bir doktora ait tüm reçeteleri bulur (reçete tarihine göre en yeniden eskiye sıralı).
     * Bu, Muayene entity'si üzerinden Doktor'a (Personel) ulaşılarak yapılır.
     *
     * @param doktorPersonelId Doktorun Personel ID'si.
     * @return Belirtilen doktora ait reçetelerin listesi.
     */
    List<Recete> findByMuayene_Doktor_IdOrderByReceteTarihiDesc(Integer doktorPersonelId);
    
    /**
     * Bir muayene ID'sine göre reçetenin var olup olmadığını kontrol eder.
     */
    boolean existsByMuayene_Id(Integer muayeneId);

    // @Query örneği ileride kullanılabilir:
    // @Query("SELECT r FROM Recete r JOIN r.receteIlaclari ri JOIN ri.ilac i WHERE i.ad = :ilacAdi")
    // List<Recete> findByIlacAdi(@Param("ilacAdi") String ilacAdi);
}
