package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Ilac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // List importu eklendi
import java.util.Optional;

@Repository
public interface IlacRepository extends JpaRepository<Ilac, Integer> {

    /**
     * Bir ilacı adıyla bulur. İlaç adının unique olduğunu varsayıyoruz.
     *
     * @param ad Aranacak ilacın adı.
     * @return Bulunursa Ilac nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<Ilac> findByAd(String ad);

    /**
     * Verilen isme sahip bir ilacın veritabanında olup olmadığını kontrol eder.
     *
     * @param ad Kontrol edilecek ilacın adı.
     * @return İlaç varsa true, yoksa false döner.
     */
    boolean existsByAd(String ad);

    /**
     * Adında belirli bir anahtar kelime geçen ilaçları bulur (büyük/küçük harf duyarsız).
     *
     * @param adKeyword Aranan anahtar kelime.
     * @return Eşleşen ilaçların listesi.
     */
    List<Ilac> findByAdContainingIgnoreCase(String adKeyword);

}