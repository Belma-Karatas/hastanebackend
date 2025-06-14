package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Yatak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YatakRepository extends JpaRepository<Yatak, Integer> {

    /**
     * Belirli bir odaya ait tüm yatakları bulur.
     *
     * @param odaId Oda ID'si.
     * @return Belirtilen odaya ait yatakların listesi.
     */
    List<Yatak> findByOda_Id(Integer odaId);

    /**
     * Belirli bir odada, verilen yatak numarasına sahip yatağı bulur.
     * Yatak entity'sindeki unique constraint (oda_id, yatak_numarasi) nedeniyle Optional döner.
     *
     * @param odaId Oda ID'si.
     * @param yatakNumarasi Aranacak yatak numarası.
     * @return Bulunursa Yatak nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<Yatak> findByOda_IdAndYatakNumarasi(Integer odaId, String yatakNumarasi);

    /**
     * Verilen yatak numarasının belirli bir odada zaten var olup olmadığını kontrol eder.
     *
     * @param odaId Oda ID'si.
     * @param yatakNumarasi Kontrol edilecek yatak numarası.
     * @return Yatak numarası o odada varsa true, yoksa false döner.
     */
    boolean existsByOda_IdAndYatakNumarasi(Integer odaId, String yatakNumarasi);

    /**
     * Belirli bir odadaki boş yatakları listeler.
     *
     * @param odaId Oda ID'si.
     * @return Belirtilen odadaki boş yatakların listesi.
     */
    List<Yatak> findByOda_IdAndDoluMuFalse(Integer odaId);

    /**
     * Belirli bir odadaki dolu yatakları listeler.
     *
     * @param odaId Oda ID'si.
     * @return Belirtilen odadaki dolu yatakların listesi.
     */
    List<Yatak> findByOda_IdAndDoluMuTrue(Integer odaId);

    /**
     * Tüm boş yatakları (hastanenin genelindeki) listeler.
     *
     * @return Tüm boş yatakların listesi.
     */
    List<Yatak> findByDoluMuFalse();

    /**
     * Bir odadaki boş yatak sayısını döndürür.
     * @param odaId Oda ID'si
     * @return Boş yatak sayısı
     */
    @Query("SELECT COUNT(y) FROM Yatak y WHERE y.oda.id = :odaId AND y.doluMu = false")
    long countByOda_IdAndDoluMuFalse(@Param("odaId") Integer odaId);

     /**
     * Bir odada belirli bir yatağın olup olmadığını ve boş olup olmadığını kontrol eder.
     * Bu, bir hastayı yatırırken belirli bir yatağın uygun olup olmadığını kontrol etmek için kullanılabilir.
     * @param odaId Oda ID'si
     * @param yatakId Yatak ID'si
     * @return Yatak varsa ve boşsa true, aksi halde false.
     */
    Optional<Yatak> findByIdAndOda_IdAndDoluMuFalse(Integer yatakId, Integer odaId);
}