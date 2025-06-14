package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.ReceteIlac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceteIlacRepository extends JpaRepository<ReceteIlac, Integer> {

    /**
     * Belirli bir reçeteye ait tüm ilaç kayıtlarını (ReceteIlac) bulur.
     *
     * @param receteId Reçete ID'si.
     * @return Belirtilen reçeteye ait ReceteIlac nesnelerinin listesi.
     */
    List<ReceteIlac> findByRecete_Id(Integer receteId);

    /**
     * Belirli bir ilacın geçtiği tüm reçete kayıtlarını (ReceteIlac) bulur.
     * Bu, bir ilacın hangi reçetelerde kullanıldığını görmek için faydalı olabilir.
     *
     * @param ilacId İlaç ID'si.
     * @return Belirtilen ilaca ait ReceteIlac nesnelerinin listesi.
     */
    List<ReceteIlac> findByIlac_Id(Integer ilacId);

    /**
     * Belirli bir reçete ve ilaç kombinasyonuna ait ReceteIlac kaydını bulur.
     * Bu, bir ilacın belirli bir reçetede zaten var olup olmadığını kontrol etmek için kullanılabilir.
     *
     * @param receteId Reçete ID'si.
     * @param ilacId İlaç ID'si.
     * @return Bulunursa ReceteIlac nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<ReceteIlac> findByRecete_IdAndIlac_Id(Integer receteId, Integer ilacId);

    /**
     * Belirli bir ilacın herhangi bir reçetede kullanılıp kullanılmadığını kontrol eder.
     * Bu, bir ilacı silmeden önce kontrol etmek için kullanılabilir.
     *
     * @param ilacId Kontrol edilecek ilaç ID'si.
     * @return İlaç herhangi bir reçetede kullanılıyorsa true, aksi halde false.
     */
    boolean existsByIlac_Id(Integer ilacId);

    /**
     * Belirli bir reçetede herhangi bir ilaç olup olmadığını kontrol eder.
     *
     * @param receteId Kontrol edilecek reçete ID'si.
     * @return Reçetede ilaç varsa true, aksi halde false.
     */
    boolean existsByRecete_Id(Integer receteId);

}