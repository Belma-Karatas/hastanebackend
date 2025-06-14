package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.PersonelVardiya;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PersonelVardiyaRepository extends JpaRepository<PersonelVardiya, Integer> {

    /**
     * Belirli bir personele ait belirli bir tarihteki vardiya atamasını bulur.
     * unique constraint (personel_id, tarih) nedeniyle Optional döner.
     *
     * @param personelId Personel ID'si.
     * @param tarih      Tarih.
     * @return Bulunursa PersonelVardiya nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<PersonelVardiya> findByPersonel_IdAndTarih(Integer personelId, LocalDate tarih);

    /**
     * Belirli bir personelin belirli bir tarihte vardiyası olup olmadığını kontrol eder.
     *
     * @param personelId Personel ID'si.
     * @param tarih      Tarih.
     * @return Vardiyası varsa true, yoksa false döner.
     */
    boolean existsByPersonel_IdAndTarih(Integer personelId, LocalDate tarih);

    /**
     * Belirli bir personele ait tüm vardiya atamalarını tarihe göre sıralı bulur.
     *
     * @param personelId Personel ID'si.
     * @return Belirtilen personele ait vardiya atamalarının listesi.
     */
    List<PersonelVardiya> findByPersonel_IdOrderByTarihDesc(Integer personelId);

    /**
     * Belirli bir vardiyaya atanmış tüm personel vardiya kayıtlarını bulur.
     * Bu, bir vardiya silinmeden önce kontrol için kullanılabilir.
     *
     * @param vardiyaId Vardiya ID'si.
     * @return Belirtilen vardiyaya ait personel vardiya atamalarının listesi.
     */
    List<PersonelVardiya> findByVardiya_Id(Integer vardiyaId);

    /**
     * Belirli bir vardiyanın herhangi bir personel vardiya atamasında kullanılıp kullanılmadığını kontrol eder.
     * @param vardiyaId Vardiya ID'si.
     * @return Kullanılıyorsa true, aksi halde false.
     */
    boolean existsByVardiya_Id(Integer vardiyaId);

    /**
     * Belirli bir tarihteki tüm personel vardiya atamalarını listeler.
     *
     * @param tarih Tarih.
     * @return Belirtilen tarihteki tüm personel vardiya atamalarının listesi.
     */
    List<PersonelVardiya> findByTarihOrderByPersonel_AdAsc(LocalDate tarih);

    /**
     * Belirli bir tarih aralığındaki ve belirli bir personele ait vardiya atamalarını listeler.
     *
     * @param personelId      Personel ID'si.
     * @param baslangicTarihi Başlangıç tarihi (dahil).
     * @param bitisTarihi     Bitiş tarihi (dahil).
     * @return Belirtilen kriterlere uyan personel vardiya atamalarının listesi.
     */
    List<PersonelVardiya> findByPersonel_IdAndTarihBetweenOrderByTarihAsc(
            Integer personelId, LocalDate baslangicTarihi, LocalDate bitisTarihi);

    /**
     * Belirli bir tarih aralığındaki tüm personel vardiya atamalarını listeler.
     * Raporlama veya genel bir bakış için kullanılabilir.
     *
     * @param baslangicTarihi Başlangıç tarihi (dahil).
     * @param bitisTarihi     Bitiş tarihi (dahil).
     * @return Belirtilen tarih aralığındaki tüm personel vardiya atamalarının listesi.
     */
    List<PersonelVardiya> findByTarihBetweenOrderByTarihAscPersonel_AdAsc(
            LocalDate baslangicTarihi, LocalDate bitisTarihi);

}