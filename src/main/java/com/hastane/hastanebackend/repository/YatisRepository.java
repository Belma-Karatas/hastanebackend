package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Yatis;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query; // Eğer özel sorgu gerekirse
// import org.springframework.data.repository.query.Param; // Eğer özel sorgu gerekirse
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface YatisRepository extends JpaRepository<Yatis, Integer> {

    /**
     * Belirli bir hastanın tüm yatış kayıtlarını (hem aktif hem geçmiş) giriş tarihine göre sıralı bulur.
     *
     * @param hastaId Hasta ID'si.
     * @return Belirtilen hastaya ait yatışların listesi.
     */
    List<Yatis> findByHasta_IdOrderByGirisTarihiDesc(Integer hastaId);

    /**
     * Belirli bir hastanın aktif (çıkış tarihi null olan) yatış kaydını bulur.
     * Bir hastanın aynı anda birden fazla aktif yatışı olmamalıdır.
     *
     * @param hastaId Hasta ID'si.
     * @return Aktif yatış varsa Yatis nesnesini içeren bir Optional, yoksa boş bir Optional.
     */
    Optional<Yatis> findByHasta_IdAndCikisTarihiIsNull(Integer hastaId);

    /**
     * Belirli bir yatakta aktif (çıkış tarihi null olan) bir yatış olup olmadığını kontrol eder.
     *
     * @param yatakId Yatak ID'si.
     * @return Aktif yatış varsa Yatis nesnesini içeren bir Optional, yoksa boş bir Optional.
     */
    Optional<Yatis> findByYatak_IdAndCikisTarihiIsNull(Integer yatakId);

    /**
     * Belirli bir sorumlu doktora ait tüm yatışları (aktif ve geçmiş) listeler.
     *
     * @param doktorPersonelId Sorumlu doktorun Personel ID'si.
     * @return Belirtilen doktora ait yatışların listesi.
     */
    List<Yatis> findBySorumluDoktor_IdOrderByGirisTarihiDesc(Integer doktorPersonelId);

    /**
     * Belirli bir tarih aralığında giriş yapmış tüm hastaların yatışlarını listeler.
     *
     * @param baslangicTarihi Başlangıç tarihi ve saati.
     * @param bitisTarihi Bitiş tarihi ve saati.
     * @return Belirtilen tarih aralığında giriş yapmış yatışların listesi.
     */
    List<Yatis> findByGirisTarihiBetween(LocalDateTime baslangicTarihi, LocalDateTime bitisTarihi);

    /**
     * Halen hastanede yatan (çıkış tarihi null olan) tüm hastaların yatışlarını listeler.
     *
     * @return Aktif yatışların listesi.
     */
    List<Yatis> findByCikisTarihiIsNullOrderByGirisTarihiDesc();


    /**
     * Belirli bir hastanın belirli bir yatakta aktif bir yatışı olup olmadığını kontrol eder.
     * @param hastaId Hasta ID'si
     * @param yatakId Yatak ID'si
     * @return Aktif yatış varsa true, yoksa false.
     */
    boolean existsByHasta_IdAndYatak_IdAndCikisTarihiIsNull(Integer hastaId, Integer yatakId);

    // --- YENİ EKLENEN METOT ---
    /**
     * Belirli bir durumdaki ve henüz çıkış yapmamış (cikisTarihi null olan)
     * yatış kayıtlarını giriş tarihine göre tersten sıralı olarak bulur.
     * Bu metot, özellikle "YATAK BEKLIYOR" veya "AKTIF" gibi durumlardaki yatışları filtrelemek için kullanılır.
     *
     * @param durum Aranacak yatış durumu (örn: "AKTIF", "YATAK BEKLIYOR").
     * @return Eşleşen ve çıkış tarihi null olan yatışların listesi.
     */
    List<Yatis> findByDurumAndCikisTarihiIsNullOrderByGirisTarihiDesc(String durum);
    // --- YENİ EKLENEN METOT SONU ---
}