package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.AcilDurumKaydi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AcilDurumKaydiRepository extends JpaRepository<AcilDurumKaydi, Integer> {

    /**
     * Belirli bir durumdaki tüm acil durum kayıtlarını olay zamanına göre sıralı bulur.
     *
     * @param durum Acil durumun durumu (örn: "AKTIF", "SONLANDIRILDI").
     * @return Belirtilen durumdaki acil durum kayıtlarının listesi.
     */
    List<AcilDurumKaydi> findByDurumOrderByOlayZamaniDesc(String durum);

    /**
     * Belirli bir hastaya ait tüm acil durum kayıtlarını olay zamanına göre sıralı bulur.
     *
     * @param hastaId Hasta ID'si.
     * @return Belirtilen hastaya ait acil durum kayıtlarının listesi.
     */
    List<AcilDurumKaydi> findByHasta_IdOrderByOlayZamaniDesc(Integer hastaId);

    /**
     * Belirli bir personel tarafından tetiklenmiş/kaydedilmiş tüm acil durum kayıtlarını bulur.
     *
     * @param personelId Tetikleyen personelin ID'si.
     * @return Belirtilen personel tarafından tetiklenmiş kayıtların listesi.
     */
    List<AcilDurumKaydi> findByTetikleyenPersonel_IdOrderByOlayZamaniDesc(Integer personelId);

    /**
     * Belirli bir tarih aralığında meydana gelmiş tüm acil durum kayıtlarını listeler.
     *
     * @param baslangicZamani Başlangıç tarihi ve saati.
     * @param bitisZamani     Bitiş tarihi ve saati.
     * @return Belirtilen tarih aralığındaki acil durum kayıtlarının listesi.
     */
    List<AcilDurumKaydi> findByOlayZamaniBetweenOrderByOlayZamaniDesc(LocalDateTime baslangicZamani, LocalDateTime bitisZamani);

    /**
     * Belirli bir konumda meydana gelmiş acil durum kayıtlarını listeler.
     *
     * @param konum Konum bilgisi (büyük/küçük harf duyarsız arama).
     * @return Belirtilen konumda meydana gelmiş kayıtların listesi.
     */
    List<AcilDurumKaydi> findByKonumContainingIgnoreCaseOrderByOlayZamaniDesc(String konum);
}