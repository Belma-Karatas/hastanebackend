package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.IzinTalep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IzinTalepRepository extends JpaRepository<IzinTalep, Integer> {

    /**
     * Belirli bir personele ait tüm izin taleplerini talep tarihine göre sıralı bulur.
     *
     * @param personelId Talep eden personelin ID'si.
     * @return Belirtilen personele ait izin taleplerinin listesi.
     */
    List<IzinTalep> findByTalepEdenPersonel_IdOrderByTalepTarihiDesc(Integer personelId);

    /**
     * Belirli bir izin türüne ait tüm izin taleplerini bulur.
     * Bu, bir izin türü silinmeden önce kontrol için kullanılabilir.
     *
     * @param izinTuruId İzin Türü ID'si.
     * @return Belirtilen izin türüne ait taleplerin listesi.
     */
    List<IzinTalep> findByIzinTuru_Id(Integer izinTuruId);

    /**
     * Belirli bir izin türünün herhangi bir izin talebinde kullanılıp kullanılmadığını kontrol eder.
     * @param izinTuruId İzin Türü ID'si.
     * @return Kullanılıyorsa true, aksi halde false.
     */
    boolean existsByIzinTuru_Id(Integer izinTuruId);


    /**
     * Belirli bir durumdaki tüm izin taleplerini listeler (örn: "BEKLIYOR").
     *
     * @param durum İzin talebinin durumu.
     * @return Belirtilen durumdaki izin taleplerinin listesi.
     */
    List<IzinTalep> findByDurumOrderByTalepTarihiDesc(String durum);

    /**
     * Belirli bir yönetici tarafından onaylanmış/reddedilmiş tüm izin taleplerini listeler.
     *
     * @param yoneticiPersonelId Onaylayan yöneticinin Personel ID'si.
     * @return Belirtilen yöneticiye ait işlem görmüş taleplerin listesi.
     */
    List<IzinTalep> findByOnaylayanYonetici_IdOrderByOnayTarihiDesc(Integer yoneticiPersonelId);

    /**
     * Belirli bir tarih aralığında başlayan izin taleplerini listeler.
     *
     * @param baslangic Başlangıç tarihi (dahil).
     * @param bitis Bitiş tarihi (dahil).
     * @return Belirtilen tarih aralığında başlayan izin taleplerinin listesi.
     */
    List<IzinTalep> findByBaslangicTarihiBetween(LocalDate baslangic, LocalDate bitis);

    /**
     * Belirli bir personelin, belirli bir tarih aralığında çakışan (onaylanmış veya bekleyen)
     * başka bir izin talebi olup olmadığını kontrol eder.
     * Bu, yeni izin talebi oluşturulurken çakışmaları önlemek için kullanılabilir.
     *
     * @param personelId Personel ID'si
     * @param baslangicTarihi Yeni iznin başlangıç tarihi
     * @param bitisTarihi Yeni iznin bitiş tarihi
     * @return Çakışan onaylanmış veya bekleyen izin varsa liste döner.
     */
    @Query("SELECT it FROM IzinTalep it WHERE it.talepEdenPersonel.id = :personelId " +
           "AND it.durum IN ('ONAYLANDI', 'BEKLIYOR') " +
           "AND ((it.baslangicTarihi <= :bitisTarihi AND it.bitisTarihi >= :baslangicTarihi))")
    List<IzinTalep> findCakisanIzinler(@Param("personelId") Integer personelId,
                                      @Param("baslangicTarihi") LocalDate baslangicTarihi,
                                      @Param("bitisTarihi") LocalDate bitisTarihi);

}