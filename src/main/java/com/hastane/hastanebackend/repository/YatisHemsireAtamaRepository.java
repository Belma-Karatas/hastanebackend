package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.YatisHemsireAtama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // EKLENDİ
import org.springframework.data.repository.query.Param; // EKLENDİ
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YatisHemsireAtamaRepository extends JpaRepository<YatisHemsireAtama, Integer> {

    /**
     * Belirli bir yatışa atanmış tüm hemşire atamalarını bulur.
     *
     * @param yatisId Yatış ID'si.
     * @return Belirtilen yatışa ait YatisHemsireAtama nesnelerinin listesi.
     */
    List<YatisHemsireAtama> findByYatis_Id(Integer yatisId);

    /**
     * Belirli bir hemşireye (Personel ID ile) yapılmış tüm yatış atamalarını bulur.
     *
     * @param hemsirePersonelId Hemşirenin Personel ID'si.
     * @return Belirtilen hemşireye ait YatisHemsireAtama nesnelerinin listesi.
     */
    List<YatisHemsireAtama> findByHemsire_Id(Integer hemsirePersonelId);

    /**
     * Belirli bir yatış ve belirli bir hemşire için yapılmış atamayı bulur.
     * Bu, bir hemşirenin bir yatışa zaten atanıp atanmadığını kontrol etmek için kullanılabilir.
     * uniqueConstraint nedeniyle Optional döner.
     *
     * @param yatisId Yatış ID'si.
     * @param hemsirePersonelId Hemşirenin Personel ID'si.
     * @return Bulunursa YatisHemsireAtama nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<YatisHemsireAtama> findByYatis_IdAndHemsire_Id(Integer yatisId, Integer hemsirePersonelId);

    /**
     * Belirli bir yatışta, belirli bir hemşirenin atanmış olup olmadığını kontrol eder.
     *
     * @param yatisId Yatış ID'si.
     * @param hemsirePersonelId Hemşirenin Personel ID'si.
     * @return Atama varsa true, yoksa false döner.
     */
    boolean existsByYatis_IdAndHemsire_Id(Integer yatisId, Integer hemsirePersonelId);

    // --- YENİ EKLENEN METOT ---
    /**
     * Belirli bir hemşireye atanmış ve hastası henüz taburcu olmamış (aktif yatışta olan)
     * YatisHemsireAtama kayıtlarını getirir. Sonuçlar yatışın giriş tarihine göre tersten sıralanır.
     *
     * @param hemsirePersonelId Hemşirenin Personel ID'si.
     * @return Aktif yatışlara atanmış hemşire kayıtlarının listesi.
     */
    @Query("SELECT yha FROM YatisHemsireAtama yha WHERE yha.hemsire.id = :hemsirePersonelId AND yha.yatis.cikisTarihi IS NULL ORDER BY yha.yatis.girisTarihi DESC")
    List<YatisHemsireAtama> findByHemsire_IdAndYatis_CikisTarihiIsNullOrderByYatis_GirisTarihiDesc(@Param("hemsirePersonelId") Integer hemsirePersonelId);
    // --- YENİ EKLENEN METOT SONU ---
}