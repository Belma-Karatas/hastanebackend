package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Oda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OdaRepository extends JpaRepository<Oda, Integer> {

    /**
     * Belirli bir kata ait tüm odaları bulur.
     *
     * @param katId Kat ID'si.
     * @return Belirtilen kata ait odaların listesi.
     */
    List<Oda> findByKat_Id(Integer katId);

    /**
     * Belirli bir katta, verilen oda numarasına sahip odayı bulur.
     * Oda entity'sindeki unique constraint (kat_id, oda_numarasi) nedeniyle Optional döner.
     *
     * @param katId Kat ID'si.
     * @param odaNumarasi Aranacak oda numarası.
     * @return Bulunursa Oda nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<Oda> findByKat_IdAndOdaNumarasi(Integer katId, String odaNumarasi);

    /**
     * Verilen oda numarasının belirli bir katta zaten var olup olmadığını kontrol eder.
     *
     * @param katId Kat ID'si.
     * @param odaNumarasi Kontrol edilecek oda numarası.
     * @return Oda numarası o katta varsa true, yoksa false döner.
     */
    boolean existsByKat_IdAndOdaNumarasi(Integer katId, String odaNumarasi);

    /**
     * Belirli bir kapasiteye veya daha fazla kapasiteye sahip odaları listeler.
     * Örnek bir özel sorgu metodu.
     *
     * @param kapasite Minimum kapasite.
     * @return Belirtilen minimum kapasiteye sahip odaların listesi.
     */
    List<Oda> findByKapasiteGreaterThanEqual(Integer kapasite);

    /**
     * Bir odanın mevcut dolu yatak sayısını getiren örnek bir JPQL sorgusu.
     * Bu sorgu, Yatak entity'sinde 'dolu_mu' gibi bir alan olduğunu varsayar.
     * Şimdilik Yatak entity'si olmadığı için bu metodu yorumda bırakıyorum.
     * Yatak entity'sini oluşturduktan sonra bu tür sorgular eklenebilir.
     */
    /*
    @Query("SELECT COUNT(y) FROM Yatak y WHERE y.oda.id = :odaId AND y.doluMu = true")
    Integer countDoluYataklarByOdaId(@Param("odaId") Integer odaId);
    */
}