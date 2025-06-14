package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Duyuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DuyuruRepository extends JpaRepository<Duyuru, Integer> {

    // Yayın tarihine göre en yeniden eskiye sıralı tüm duyurular
    List<Duyuru> findAllByOrderByYayinTarihiDesc();

    // Belirli bir personel tarafından yayınlanan duyurular
    List<Duyuru> findByYayinlayanPersonel_IdOrderByYayinTarihiDesc(Integer personelId);

    // Belirli bir tarih aralığında yayınlanmış duyurular
    List<Duyuru> findByYayinTarihiBetweenOrderByYayinTarihiDesc(LocalDateTime baslangic, LocalDateTime bitis);

    // Başlıkta veya içerikte arama (içerik alanı CLOB değilse çalışır)
    List<Duyuru> findByBaslikContainingIgnoreCaseOrIcerikContainingIgnoreCaseOrderByYayinTarihiDesc(String baslikKeyword, String icerikKeyword);
}
